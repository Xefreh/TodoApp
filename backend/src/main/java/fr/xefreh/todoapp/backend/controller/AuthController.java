package fr.xefreh.todoapp.backend.controller;

import fr.xefreh.todoapp.backend.dto.AuthResponseDto;
import fr.xefreh.todoapp.backend.dto.CredentialsDto;
import fr.xefreh.todoapp.backend.service.AuthResult;
import fr.xefreh.todoapp.backend.service.AuthService;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

/**
 * Authentication endpoints. Two handlers to register:
 * <ul>
 *   <li>{@link #Register} on {@code POST /api/auth/register}</li>
 *   <li>{@link #Login} on {@code POST /api/auth/login}</li>
 * </ul>
 *
 * Domain exceptions ({@code UsernameTakenException}, {@code InvalidCredentialsException})
 * bubble up to the exception handler in {@code Main}, which maps them to the HTTP status
 * and error name they carry. Instances are created by passing the {@link AuthService}
 * built at bootstrap.
 */
public final class AuthController {

    /** Username column limit — validated before hitting the DB. */
    private static final int USERNAME_MAX_LENGTH = 255;

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Handler for {@code POST /api/auth/register}. */
    public final Handler Register = new Handler() {
        @Override
        public void handle(@NotNull Context ctx) {
            CredentialsDto credentials = parseCredentials(ctx);
            AuthResult result = authService.register(credentials.username, credentials.password);
            ctx.status(201).json(toDto(result));
        }
    };

    /** Handler for {@code POST /api/auth/login}. */
    public final Handler Login = new Handler() {
        @Override
        public void handle(@NotNull Context ctx) {
            CredentialsDto credentials = parseCredentials(ctx);
            AuthResult result = authService.login(credentials.username, credentials.password);
            ctx.status(200).json(toDto(result));
        }
    };

    /**
     * Parses and validates the credentials body. Throws {@link BadRequestResponse} (400) on
     * missing/malformed JSON or blank fields — interrupting the chain instead of letting the
     * handler run with invalid input (which previously ended in a 500).
     *
     * <p>Note: catches {@link Exception} and not just {@link RuntimeException}: Javalin is
     * written in Kotlin, which does not enforce checked exceptions — Jackson's
     * {@code JsonProcessingException} (an {@code IOException}) escapes {@code bodyAsClass}
     * undeclared.</p>
     */
    private static CredentialsDto parseCredentials(Context ctx) {
        CredentialsDto credentials;
        try {
            credentials = ctx.bodyAsClass(CredentialsDto.class);
        } catch (Exception e) {
            throw new BadRequestResponse("Request body must be a JSON credentials object");
        }
        if (credentials == null || credentials.username == null || credentials.username.isBlank()
                || credentials.password == null || credentials.password.isBlank()) {
            throw new BadRequestResponse("username and password are required");
        }
        if (credentials.username.length() > USERNAME_MAX_LENGTH) {
            throw new BadRequestResponse("username must be at most " + USERNAME_MAX_LENGTH + " characters");
        }
        return credentials;
    }

    private static AuthResponseDto toDto(AuthResult result) {
        return new AuthResponseDto(result.token(), result.userId());
    }
}
