package fr.xefreh.todoapp.backend;

import fr.xefreh.todoapp.backend.config.JpaConfig;
import fr.xefreh.todoapp.backend.controller.AuthController;
import fr.xefreh.todoapp.backend.repository.JpaUserRepository;
import fr.xefreh.todoapp.backend.security.AuthFilter;
import fr.xefreh.todoapp.backend.service.Argon2PasswordHasher;
import fr.xefreh.todoapp.backend.service.AuthException;
import fr.xefreh.todoapp.backend.service.AuthServiceImpl;
import fr.xefreh.todoapp.backend.service.JwtTokenService;
import fr.xefreh.todoapp.backend.service.PasswordHasher;
import fr.xefreh.todoapp.backend.service.TokenService;
import io.javalin.Javalin;

/**
 * Point d'entrée du serveur REST Javalin.
 *
 * Bootstrap la persistance (Hibernate + H2) et l'authentification (JWT + argon2id).
 * Les routes CRUD notes arrivent dans le commit suivant.
 *
 * Démarrage : {@code ./gradlew :backend:run} — écoute sur le port 7000 (toutes interfaces).
 * Depuis l'émulateur Android : {@code http://10.0.2.2:7000}.
 */
public final class Main {

    /** Port d'écoute du serveur. {@code 10.0.2.2:7000} depuis l'émulateur Android. */
    public static final int PORT = 7000;

    private Main() {
    }

    public static void main(String[] args) {
        // Initialise l'EntityManagerFactory au démarrage (crée/maj le schéma H2).
        JpaConfig.entityManagerFactory();

        // --- Construction des services (assemblage manuel des dépendances) ---
        PasswordHasher passwordHasher = new Argon2PasswordHasher();
        TokenService tokenService = new JwtTokenService();
        AuthServiceImpl authService = new AuthServiceImpl(
                new JpaUserRepository(), passwordHasher, tokenService);
        AuthController authController = new AuthController(authService);

        Javalin.create(config -> {
            // CORS permissif en développement (tests curl/browser depuis le host).
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost()));

            // Sonde de connectivité.
            config.routes.get("/api/health", ctx -> ctx.json(new HealthResponse("ok")));

            // Routes d'authentification (publiques).
            config.routes.post("/api/auth/register", authController.Register);
            config.routes.post("/api/auth/login", authController.Login);

            // Routes protégées : le filtre valide le Bearer JWT et expose le userId.
            config.routes.before("/api/notes/*", new AuthFilter(tokenService));

            // Gestion d'erreurs uniforme.
            config.routes.exception(AuthException.class, (e, ctx) -> {
                ctx.status(400);
                ctx.json(new ErrorResponse("AUTH_ERROR", e.getMessage()));
            });
            config.routes.exception(Exception.class, (e, ctx) -> {
                ctx.status(500);
                ctx.json(new ErrorResponse("INTERNAL_ERROR", e.getMessage()));
            });
        }).start(PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(JpaConfig::shutdown));

        System.out.println("TodoApp backend démarré sur http://0.0.0.0:" + PORT);
    }

    /** Corps de réponse de la sonde de santé. */
    public record HealthResponse(String status) {
    }

    /** Corps d'erreur JSON standard. */
    public record ErrorResponse(String error, String message) {
    }
}
