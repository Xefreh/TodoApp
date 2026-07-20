package fr.xefreh.todoapp.backend.service;

/**
 * Result of a successful registration or login: the JWT token to use for authenticated
 * requests, and the user id.
 */
public record AuthResult(String token, long userId) {
}
