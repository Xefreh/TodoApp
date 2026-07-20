package fr.xefreh.todoapp.backend.service;

/**
 * Thrown when a requested note does not exist or does not belong to the user.
 * The controller translates it into HTTP 404 (rather than 403 to avoid leaking the
 * existence of a note owned by someone else).
 */
public class NoteNotFoundException extends RuntimeException {
    public NoteNotFoundException(long id) {
        super("Note not found: " + id);
    }
}
