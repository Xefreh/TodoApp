package fr.xefreh.todoapp.data;

/**
 * Levée par {@link NotesRepository} (et consorts) en cas d'échec de communication avec
 * l'API REST : erreur réseau, code HTTP >= 400, body illisible. Porte le code HTTP quand
 * disponible (−1 pour une erreur purement réseau).
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
