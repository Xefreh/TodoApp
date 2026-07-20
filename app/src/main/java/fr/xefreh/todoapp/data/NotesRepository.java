package fr.xefreh.todoapp.data;

import fr.xefreh.todoapp.Note;
import java.util.List;

/**
 * Unified access to notes: source of truth = server (via {@link TodoApi}), with a local cache
 * (via Room). Interface kept separate from {@link NotesRepositoryImpl} so it can be tested with
 * Mockito mocks ({@code TodoApi} + {@code NoteDao} are themselves interfaces).
 *
 * <p>Methods are <b>blocking</b> and must be called off the main thread (e.g. via an
 * {@code ExecutorService}). They throw an {@link ApiException} on network failure or HTTP error.</p>
 */
public interface NotesRepository {

    /**
     * Reloads all notes from the server and replaces the local cache.
     * @return the list of notes (never null).
     */
    List<Note> fetchAll();

    /**
     * Creates a note on the server then inserts it into the local cache.
     * @return the created note (with server id and createdAt).
     */
    Note create(String title, String body, String imageUri);

    /**
     * Updates a note on the server then in the local cache.
     */
    void update(Note note);

    /**
     * Deletes a note on the server then from the local cache.
     */
    void delete(long serverId);
}
