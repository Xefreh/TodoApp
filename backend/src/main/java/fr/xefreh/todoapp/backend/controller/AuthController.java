package fr.xefreh.todoapp.backend.controller;

import fr.xefreh.todoapp.backend.dto.AuthResponseDto;
import fr.xefreh.todoapp.backend.dto.CredentialsDto;
import fr.xefreh.todoapp.backend.service.AuthResult;
import fr.xefreh.todoapp.backend.service.AuthService;
import fr.xefreh.todoapp.backend.service.InvalidCredentialsException;
import fr.xefreh.todoapp.backend.service.UsernameTakenException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

/**
 * Endpoints d'authentification. Deux handlers à enregistrer :
 * <ul>
 *   <li>{@link #Register} sur {@code POST /api/auth/register}</li>
 *   <li>{@link #Login} sur {@code POST /api/auth/login}</li>
 * </ul>
 *
 * Les instances sont créées en passant l'{@link AuthService} construit au bootstrap.
 */
public final class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Handler {@code POST /api/auth/register}. */
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

    /** Handler {@code POST /api/auth/login}. */
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

    private static CredentialsDto parseCredentials(Context ctx) {
        CredentialsDto credentials = ctx.bodyAsClass(CredentialsDto.class);
        if (credentials.username == null || credentials.username.isBlank()
                || credentials.password == null || credentials.password.isBlank()) {
            ctx.status(400).json(new ErrorBody("BAD_REQUEST", "username and password are required"));
            return new CredentialsDto();
        }
        return credentials;
    }

    private static AuthResponseDto toDto(AuthResult result) {
        return new AuthResponseDto(result.token(), result.userId());
    }

    /** Corps d'erreur JSON standard. */
    public record ErrorBody(String error, String message) {
    }
}
