package fr.xefreh.todoapp.backend.service;

/**
 * Exception de base pour les erreurs d'authentification. Les sous-classes permettent
 * au contrôleur de renvoyer le bon code HTTP (409 pour un conflit, 401 pour des
 * identifiants invalides).
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
