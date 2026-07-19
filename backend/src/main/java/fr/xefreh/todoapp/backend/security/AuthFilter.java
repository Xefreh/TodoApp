package fr.xefreh.todoapp.backend.security;

import fr.xefreh.todoapp.backend.service.InvalidTokenException;
import fr.xefreh.todoapp.backend.service.TokenService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

/**
 * Pré-filtre d'authentification. À enregistrer via {@code before("/api/notes/*", new AuthFilter(...))}.
 *
 * Extrait le jeton de l'en-tête {@code Authorization: Bearer <token>}, le vérifie via le
 * {@link TokenService}, et expose l'identifiant utilisateur au reste de la requête via
 * {@link Context#attribute(String, Object)} sous la clé {@link #USER_ID_KEY}.
 *
 * Si le jeton est absent ou invalide, répond 401 et stoppe la requête.
 */
public class AuthFilter implements Handler {

    /** Clé sous laquelle l'identifiant utilisateur est exposé dans le contexte Javalin. */
    public static final String USER_ID_KEY = "authenticatedUserId";

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;

    public AuthFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void handle(@NotNull Context ctx) {
        String header = ctx.header(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            ctx.status(401).json(new ErrorBody("UNAUTHORIZED", "Missing or invalid Authorization header"));
            return;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        try {
            long userId = tokenService.verify(token);
            ctx.attribute(USER_ID_KEY, userId);
        } catch (InvalidTokenException e) {
            ctx.status(401).json(new ErrorBody("UNAUTHORIZED", e.getMessage()));
        }
    }

    /** Corps d'erreur JSON standard pour les réponses 401. */
    public record ErrorBody(String error, String message) {
    }
}
