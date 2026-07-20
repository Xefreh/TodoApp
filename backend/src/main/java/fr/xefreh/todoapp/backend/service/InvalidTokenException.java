package fr.xefreh.todoapp.backend.service;

/**
 * Thrown when a JWT token is missing, malformed, expired or invalid. The authentication
 * filter translates it into HTTP 401.
 */
public class InvalidTokenException extends AuthException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
