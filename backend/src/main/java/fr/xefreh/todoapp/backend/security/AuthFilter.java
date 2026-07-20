package fr.xefreh.todoapp.backend.security;

import fr.xefreh.todoapp.backend.service.InvalidTokenException;
import fr.xefreh.todoapp.backend.service.TokenService;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Authentication pre-filter. Register via {@code before("/api/notes", ...)} and
 * {@code before("/api/notes/*", ...)}.
 *
 * <p>Extracts the token from the {@code Authorization: Bearer <token>} header, verifies it
 * via the {@link TokenService}, and exposes the user id to the rest of the request via
 * {@link io.javalin.http.Context#attribute(String, Object)} under the key
 * {@link #USER_ID_KEY}.</p>
 *
 * <p>On failure, throws an {@link UnauthorizedResponse} — Javalin interrupts the chain
 * and returns 401 automatically (the {@code HttpResponseException} handler defined in
 * {@code Main} produces the standard JSON body).</p>
 */
public class AuthFilter implements Handler {

    /** Key under which the user id is exposed in the Javalin context. */
    public static final String USER_ID_KEY = "authenticatedUserId";

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;

    public AuthFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void handle(@NotNull io.javalin.http.Context ctx) {
        String header = ctx.header(AUTH_HEADER);
        String token = null;
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            token = header.substring(BEARER_PREFIX.length()).trim();
        }
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedResponse("Missing or invalid Authorization header");
        }
        try {
            long userId = tokenService.verify(token);
            ctx.attribute(USER_ID_KEY, userId);
        } catch (InvalidTokenException e) {
            throw new UnauthorizedResponse(e.getMessage());
        }
    }
}
