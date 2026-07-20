package fr.xefreh.todoapp.backend;

import fr.xefreh.todoapp.backend.config.JpaConfig;
import fr.xefreh.todoapp.backend.controller.AuthController;
import fr.xefreh.todoapp.backend.controller.NoteController;
import fr.xefreh.todoapp.backend.repository.JpaNoteRepository;
import fr.xefreh.todoapp.backend.repository.JpaUserRepository;
import fr.xefreh.todoapp.backend.security.AuthFilter;
import fr.xefreh.todoapp.backend.service.Argon2PasswordHasher;
import fr.xefreh.todoapp.backend.service.AuthException;
import fr.xefreh.todoapp.backend.service.AuthServiceImpl;
import fr.xefreh.todoapp.backend.service.JwtTokenService;
import fr.xefreh.todoapp.backend.service.NoteServiceImpl;
import fr.xefreh.todoapp.backend.service.PasswordHasher;
import fr.xefreh.todoapp.backend.service.TokenService;
import io.javalin.Javalin;

/**
 * Entry point of the Javalin REST server.
 *
 * Bootstraps persistence (Hibernate + H2) and authentication (JWT + argon2id).
 *
 * Startup: {@code ./gradlew :backend:run} — listens on port 7000 (all interfaces).
 * From the Android emulator: {@code http://10.0.2.2:7000}.
 */
public final class Main {

    /** Server listen port. {@code 10.0.2.2:7000} from the Android emulator. */
    public static final int PORT = 7000;

    private Main() {
    }

    public static void main(String[] args) {
        // Initializes the EntityManagerFactory at startup (creates/updates the H2 schema).
        JpaConfig.entityManagerFactory();

        // --- Service wiring (manual dependency assembly) ---
        PasswordHasher passwordHasher = new Argon2PasswordHasher();
        TokenService tokenService = new JwtTokenService();
        AuthServiceImpl authService = new AuthServiceImpl(
                new JpaUserRepository(), passwordHasher, tokenService);
        AuthController authController = new AuthController(authService);

        NoteServiceImpl noteService = new NoteServiceImpl(new JpaNoteRepository());
        NoteController noteController = new NoteController(noteService);

        Javalin.create(config -> {
            // Permissive CORS in development (curl/browser tests from the host).
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost()));

            // Connectivity probe.
            config.routes.get("/api/health", ctx -> ctx.json(new HealthResponse("ok")));

            // Authentication routes (public).
            config.routes.post("/api/auth/register", authController.Register);
            config.routes.post("/api/auth/login", authController.Login);

            // Protected routes: the filter validates the Bearer JWT and exposes the userId.
            // Two patterns because "/api/notes/*" does not cover the exact "/api/notes".
            AuthFilter authFilter = new AuthFilter(tokenService);
            config.routes.before("/api/notes", authFilter);
            config.routes.before("/api/notes/*", authFilter);
            config.routes.get("/api/notes", noteController.ListNotes);
            config.routes.post("/api/notes", noteController.CreateNote);
            config.routes.get("/api/notes/{id}", noteController.GetNote);
            config.routes.put("/api/notes/{id}", noteController.UpdateNote);
            config.routes.delete("/api/notes/{id}", noteController.DeleteNote);

            // Uniform error handling.
            config.routes.exception(AuthException.class, (e, ctx) -> {
                ctx.status(400);
                ctx.json(new ErrorResponse("AUTH_ERROR", e.getMessage()));
            });
            // HttpResponseException (UnauthorizedResponse, etc.): honors the carried status.
            config.routes.exception(io.javalin.http.HttpResponseException.class, (e, ctx) -> {
                ctx.status(e.getStatus());
                ctx.json(new ErrorResponse(httpErrorName(e.getStatus()), e.getMessage()));
            });
            config.routes.exception(Exception.class, (e, ctx) -> {
                ctx.status(500);
                ctx.json(new ErrorResponse("INTERNAL_ERROR", e.getMessage()));
            });
        }).start(PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(JpaConfig::shutdown));

        System.out.println("TodoApp backend started on http://0.0.0.0:" + PORT);
    }

    /** Response body of the health probe. */
    public record HealthResponse(String status) {
    }

    /** Standard JSON error body. */
    public record ErrorResponse(String error, String message) {
    }

    /** Symbolic error name derived from the HTTP status (401 -> UNAUTHORIZED, ...). */
    private static String httpErrorName(int status) {
        return switch (status) {
            case 400 -> "BAD_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            case 422 -> "UNPROCESSABLE";
            default -> "HTTP_" + status;
        };
    }
}
