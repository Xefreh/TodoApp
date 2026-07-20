package fr.xefreh.todoapp.backend.dto;

/**
 * Standard JSON error body returned by every endpoint ({@code error} is a symbolic name
 * like {@code UNAUTHORIZED}, {@code message} is human-readable).
 */
public record ErrorBody(String error, String message) {
}
