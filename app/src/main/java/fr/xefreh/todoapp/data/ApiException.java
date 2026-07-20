package fr.xefreh.todoapp.data;

/**
 * Thrown by {@link NotesRepository} (and friends) on a communication failure with the
 * REST API: network error, HTTP code >= 400, unreadable body. Carries the HTTP code when
 * available (-1 for a purely network error).
 */
public class ApiException extends RuntimeException {

    private final int httpCode;

    public ApiException(String message, int httpCode) {
        super(message);
        this.httpCode = httpCode;
    }

    public ApiException(String message, int httpCode, Throwable cause) {
        super(message, cause);
        this.httpCode = httpCode;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
