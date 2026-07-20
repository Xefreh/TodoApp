package fr.xefreh.todoapp.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.xefreh.todoapp.Note;
import fr.xefreh.todoapp.NoteDao;
import fr.xefreh.todoapp.data.dto.NoteDto;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Unit tests for {@link NotesRepositoryImpl}. The two main dependencies
 * ({@link TodoApi} Retrofit and {@link NoteDao} Room) are interfaces mocked with
 * Mockito. The Retrofit {@link Call}s are stubbed to return a {@link Response}.
 *
 * <p>Important: always build the mock {@link Call} <b>before</b> calling
 * {@code when(api...)} to avoid an {@code UnfinishedStubbingException} (Mockito detects a
 * nested stubbing if you create/stub a mock while another stubbing is being evaluated).</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotesRepositoryImplTest {

    private static final String AUTH_HEADER = "Bearer jwt-token";

    @Mock
    private TodoApi api;
    @Mock
    private NoteDao noteDao;
    @Mock
    private SessionManager sessionManager;

    private NotesRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        when(sessionManager.authHeader()).thenReturn(AUTH_HEADER);
        repository = new NotesRepositoryImpl(api, noteDao, sessionManager);
    }

    // --- fetchAll ---

    @Test
    void fetchAll_clearsCacheAndInsertsEachMappedNote() {
        NoteDto dto1 = dto(10L, "T1", "B1", "img1", 100L);
        NoteDto dto2 = dto(20L, "T2", "B2", null, 200L);
        Call<List<NoteDto>> call = successful(List.of(dto1, dto2));
        when(api.getNotes(AUTH_HEADER)).thenReturn(call);

        List<Note> result = repository.fetchAll();

        assertEquals(2, result.size());
        Note first = result.get(0);
        assertEquals(10L, first.getId());
        assertEquals("T1", first.getTitle());
        assertEquals("img1", first.getImageUri());
        assertEquals(100L, first.getCreatedAt());

        verify(noteDao).clear();
        verify(noteDao).insert(noteWithId(10L));
        verify(noteDao).insert(noteWithId(20L));
    }

    @Test
    void fetchAll_treatsNullCreatedAtAsZero() {
        NoteDto d = dto(1L, "T", "B", null, null);
        Call<List<NoteDto>> call = successful(List.of(d));
        when(api.getNotes(AUTH_HEADER)).thenReturn(call);

        List<Note> result = repository.fetchAll();

        assertEquals(0L, result.get(0).getCreatedAt());
    }

    @Test
    void fetchAll_throwsApiExceptionOnHttpError() {
        Call<List<NoteDto>> call = failedResponse(500);
        when(api.getNotes(AUTH_HEADER)).thenReturn(call);

        ApiException e = assertThrows(ApiException.class, () -> repository.fetchAll());
        assertEquals(500, e.getHttpCode());
        verify(noteDao, never()).clear();
        verify(noteDao, never()).insert(any(Note.class));
    }

    @Test
    void fetchAll_throwsApiExceptionOnIOException() {
        Call<List<NoteDto>> call = mockThrowing(new IOException("timeout"));
        when(api.getNotes(AUTH_HEADER)).thenReturn(call);

        ApiException e = assertThrows(ApiException.class, () -> repository.fetchAll());
        assertEquals(-1, e.getHttpCode());
        verify(noteDao, never()).clear();
    }

    // --- create ---

    @Test
    void create_postsPayloadAndInsertsReturnedNote() {
        NoteDto created = dto(77L, "Title", "Body", "img", 1234L);
        Call<NoteDto> call = successful(created);
        when(api.createNote(eq(AUTH_HEADER), any(NoteDto.class))).thenReturn(call);

        Note result = repository.create("Title", "Body", "img");

        assertEquals(77L, result.getId());
        assertEquals("Title", result.getTitle());
        assertEquals(1234L, result.getCreatedAt());
        verify(noteDao).insert(noteWithId(77L));
    }

    @Test
    void create_propagatesHttpFailureWithoutLocalInsert() {
        Call<NoteDto> call = failedResponse(400);
        when(api.createNote(eq(AUTH_HEADER), any(NoteDto.class))).thenReturn(call);

        ApiException e = assertThrows(ApiException.class,
                () -> repository.create("T", "B", null));
        assertEquals(400, e.getHttpCode());
        verify(noteDao, never()).insert(any(Note.class));
    }

    // --- update ---

    @Test
    void update_putsAndReplacesLocalWithServerVersion() {
        Note local = new Note(5L, "OldT", "OldB", "oldImg", 999L);
        NoteDto serverReturned = dto(5L, "NewT", "NewB", null, 999L);
        Call<NoteDto> call = successful(serverReturned);
        when(api.updateNote(eq(AUTH_HEADER), eq(5L), any(NoteDto.class))).thenReturn(call);

        repository.update(local);

        verify(noteDao).insert(noteWithId(5L));
    }

    // --- delete ---

    @Test
    void delete_callsApiThenDeletesLocalById() {
        Call<Void> call = successful(null);
        when(api.deleteNote(AUTH_HEADER, 42L)).thenReturn(call);

        repository.delete(42L);

        verify(api).deleteNote(AUTH_HEADER, 42L);
        verify(noteDao).deleteById(42L);
    }

    @Test
    void delete_skipsLocalDeletionOnHttpFailure() {
        Call<Void> call = failedResponse(404);
        when(api.deleteNote(AUTH_HEADER, 42L)).thenReturn(call);

        assertThrows(ApiException.class, () -> repository.delete(42L));
        verify(noteDao, never()).deleteById(anyLong());
    }

    // --- auth ---

    @Test
    void fetchAll_throwsWhenNoSession() {
        when(sessionManager.authHeader()).thenReturn(null);

        ApiException e = assertThrows(ApiException.class, () -> repository.fetchAll());
        assertEquals(401, e.getHttpCode());
        verify(api, never()).getNotes(anyString());
    }

    // --- helpers ---

    private static NoteDto dto(Long id, String title, String body, String imageUri, Long createdAt) {
        NoteDto d = new NoteDto();
        d.id = id;
        d.title = title;
        d.body = body;
        d.imageUri = imageUri;
        d.createdAt = createdAt;
        return d;
    }

    /** Argument matcher checking only the id of the inserted Note. */
    private static Note noteWithId(long id) {
        return ArgumentMatchers.argThat(n -> n != null && n.getId() == id);
    }

    @SuppressWarnings("unchecked")
    private static <T> Call<T> successful(T body) {
        Call<T> call = mock(Call.class);
        try {
            doReturn(Response.success(body)).when(call).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return call;
    }

    @SuppressWarnings("unchecked")
    private static <T> Call<T> failedResponse(int code) {
        Call<T> call = mock(Call.class);
        try {
            doReturn(Response.<T>error(code, okhttp3.ResponseBody.create("", null)))
                    .when(call).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return call;
    }

    @SuppressWarnings("unchecked")
    private static <T> Call<T> mockThrowing(IOException e) {
        Call<T> call = mock(Call.class);
        try {
            doThrow(e).when(call).execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return call;
    }
}
