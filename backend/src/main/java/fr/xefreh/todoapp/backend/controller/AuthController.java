package fr.xefreh.todoapp.backend.controller;

import fr.xefreh.todoapp.backend.dto.AuthResponseDto;
import fr.xefreh.todoapp.backend.dto.CredentialsDto;
import fr.xefreh.todoapp.backend.service.AuthResult;
import fr.xefreh.todoapp.backend.service.AuthService;
import fr.xefreh.todoapp.backend.service.InvalidCredentialsException;
import fr.xefreh.todoapp.backend.service.UsernameTakenException;
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
 * Instances are created by passing the {@link AuthService} built at bootstrap.
 */
public final class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Handler for {@code POST /api/auth/register}. */
    public final Handler Register = new Handler() {
        @Override
        public void handle(@NotNull Context ctx) {
            CredentialsDto credentials = parseCredentials(ctx);
            try {
                AuthResult result = authService.register(credentials.username, credentials.password);
                ctx.status(201).json(toDto(result));
            } catch (UsernameTakenException e) {
                ctx.status(409).json(new ErrorBody("USERNAME_TAKEN", e.getMessage()));
            }
        }
    };

    /** Handler for {@code POST /api/auth/login}. */
    public final Handler Login = new Handler() {
        @Override
        public void handle(@NotNull Context ctx) {
            CredentialsDto credentials = parseCredentials(ctx);
            try {
                AuthResult result = authService.login(credentials.username, credentials.password);
                ctx.status(200).json(toDto(result));
            } catch (InvalidCredentialsException e) {
                ctx.status(401).json(new ErrorBody("INVALID_CREDENTIALS", e.getMessage()));
            }
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
        return credentials;
    }

    private static AuthResponseDto toDto(AuthResult result) {
        return new AuthResponseDto(result.token(), result.userId());
    }

    /** Standard JSON error body. */
    public record ErrorBody(String error, String message) {
    }
}
