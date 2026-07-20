package fr.xefreh.todoapp.backend.service;

/**
 * Base exception for authentication errors. Subclasses let the controller return the
 * correct HTTP code (409 for a conflict, 401 for invalid credentials).
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
