package fr.xefreh.todoapp.data;

import fr.xefreh.todoapp.Note;
import java.util.List;

/**
 * Access to notes on the REST API (via {@link TodoApi}): the server is the only source of
 * truth, nothing is persisted locally. Interface kept separate from {@link NotesRepositoryImpl}
 * so it can be tested with Mockito mocks ({@code TodoApi} is itself an interface).
 *
 * <p>Methods are <b>blocking</b> and must be called off the main thread (e.g. via an
 * {@code ExecutorService}). They throw an {@link ApiException} on network failure or HTTP error.</p>
 */
public interface NotesRepository {

    /**
     * Loads all notes of the authenticated user from the server.
     * @return the list of notes (never null).
     */
    List<Note> fetchAll();

    /**
     * Creates a note on the server.
     * @return the created note (with server id and createdAt).
     */
    Note create(String title, String body, String imageUri);

    /**
     * Updates a note on the server.
     */
    void update(Note note);

    /**
     * Deletes a note on the server.
     */
    void delete(long serverId);
}
