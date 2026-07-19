package fr.xefreh.todoapp.backend.service;

/**
 * Levée quand un jeton JWT est absent, malformé, expiré ou invalide. Le filtre
 * d'authentification la traduit en HTTP 401.
 */
public class InvalidTokenException extends AuthException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
