package fr.xefreh.todoapp.backend.security;

import fr.xefreh.todoapp.backend.service.InvalidTokenException;
import fr.xefreh.todoapp.backend.service.TokenService;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Pré-filtre d'authentification. À enregistrer via {@code before("/api/notes", ...)} et
 * {@code before("/api/notes/*", ...)}.
 *
 * <p>Extrait le jeton de l'en-tête {@code Authorization: Bearer <token>}, le vérifie via le
 * {@link TokenService}, et expose l'identifiant utilisateur au reste de la requête via
 * {@link io.javalin.http.Context#attribute(String, Object)} sous la clé
 * {@link #USER_ID_KEY}.</p>
 *
 * <p>En cas d'échec, lève une {@link UnauthorizedResponse} — Javalin interrompt la chaîne
 * et renvoie 401 automatiquement (le handler {@code HttpResponseException} défini dans
 * {@code Main} produit le corps JSON standard).</p>
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
