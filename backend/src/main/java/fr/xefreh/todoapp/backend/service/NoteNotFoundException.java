package fr.xefreh.todoapp.backend.service;

/**
 * Levée quand une note demandée n'existe pas ou n'appartient pas à l'utilisateur.
 * Le contrôleur la traduit en HTTP 404 (plutôt que 403 pour ne pas fuiter l'existence
 * d'une note appartenant à autrui).
 */
public class NoteNotFoundException extends RuntimeException {
    public NoteNotFoundException(long id) {
        super("Note not found: " + id);
    }
}
