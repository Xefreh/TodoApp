package fr.xefreh.todoapp.data;

import androidx.annotation.NonNull;

import fr.xefreh.todoapp.Note;
import fr.xefreh.todoapp.data.dto.NoteDto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Implementation of {@link NotesRepository} backed by {@link TodoApi} (network only — the
 * server is the source of truth, nothing is cached locally).
 *
 * <p>The {@code authHeader} ("Bearer &lt;token&gt;") is passed explicitly to each call even
 * though the OkHttp {@code AuthInterceptor} already injects it: this makes the implementation
 * testable without an interceptor (the {@code TodoApi} mock receives the header as a parameter).</p>
 *
 * <p>All methods are <b>blocking</b> (synchronous {@code execute()}) and throw
 * {@link ApiException} on failure. Callers must invoke them off the main thread.</p>
 */
public class NotesRepositoryImpl implements NotesRepository {

    private final TodoApi api;
    private final SessionManager sessionManager;

    public NotesRepositoryImpl(TodoApi api, SessionManager sessionManager) {
        this.api = api;
        this.sessionManager = sessionManager;
    }

    @Override
    public List<Note> fetchAll() {
        String auth = requireAuthHeader();
        List<NoteDto> dtos = execute(api.getNotes(auth));
        List<Note> notes = new ArrayList<>();
        for (NoteDto dto : dtos) {
            notes.add(toNote(dto));
        }
        return notes;
    }

    @Override
    public Note create(String title, String body, String imageUri) {
        String auth = requireAuthHeader();
        NoteDto payload = new NoteDto(title, body, imageUri);
        return toNote(execute(api.createNote(auth, payload)));
    }

    @Override
    public void update(@NonNull Note note) {
        String auth = requireAuthHeader();
        execute(api.updateNote(auth, note.getId(), toDto(note)));
    }

    @Override
    public void delete(long serverId) {
        String auth = requireAuthHeader();
        // DELETE returns an empty body (204): just check HTTP success.
        Response<Void> response;
        try {
            response = api.deleteNote(auth, serverId).execute();
        } catch (IOException e) {
            throw new ApiException("Network error: " + e.getMessage(), -1, e);
        }
        if (!response.isSuccessful()) {
            throw new ApiException("HTTP " + response.code(), response.code());
        }
    }

    // --- helpers ---

    private String requireAuthHeader() {
        String header = sessionManager.authHeader();
        if (header == null) {
            throw new ApiException("Not authenticated", 401);
        }
        return header;
    }

    /** Executes a {@link Call} synchronously and throws {@link ApiException} on failure. */
    private static <T> T execute(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (!response.isSuccessful()) {
                throw new ApiException("HTTP " + response.code(), response.code());
            }
            T body = response.body();
            if (body == null) {
                throw new ApiException("Empty response body", response.code());
            }
            return body;
        } catch (IOException e) {
            throw new ApiException("Network error: " + e.getMessage(), -1, e);
        }
    }

    private static Note toNote(NoteDto dto) {
        long createdAt = dto.createdAt == null ? 0L : dto.createdAt;
        return new Note(dto.id, dto.title, dto.body, dto.imageUri, createdAt);
    }

    private static NoteDto toDto(Note note) {
        NoteDto dto = new NoteDto();
        dto.title = note.getTitle();
        dto.body = note.getBody();
        dto.imageUri = note.getImageUri();
        return dto;
    }
}
