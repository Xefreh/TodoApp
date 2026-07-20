package fr.xefreh.todoapp.backend.service;

/**
 * Base exception for authentication errors. Each subclass carries the HTTP status and the
 * symbolic error name the API should return (409 for a conflict, 401 for invalid
 * credentials, ...), so the single exception handler registered in {@code Main} can
 * translate any {@code AuthException} without the controllers having to catch them.
 */
public class AuthException extends RuntimeException {

    private final int httpStatus;
    private final String errorName;

    protected AuthException(int httpStatus, String errorName, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorName = errorName;
    }

    /** The HTTP status to return for this error (401, 409, ...). */
    public int httpStatus() {
        return httpStatus;
    }

    /** The symbolic error name exposed in the JSON error body. */
    public String errorName() {
        return errorName;
    }
}
