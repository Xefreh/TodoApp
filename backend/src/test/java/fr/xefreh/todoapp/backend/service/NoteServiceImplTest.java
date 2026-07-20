package fr.xefreh.todoapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.xefreh.todoapp.backend.dto.NoteDto;
import fr.xefreh.todoapp.backend.model.NoteEntity;
import fr.xefreh.todoapp.backend.repository.NoteRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link NoteServiceImpl}. The {@link NoteRepository} (interface) is mocked:
 * we validate filtering by owner, entity<->DTO mapping, assignment of createdAt, and its
 * immutability on update.
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    private static final long OWNER = 1L;
    private static final long OTHER = 2L;

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    @Test
    void listForOwner_mapsEntitiesToDtos() {
        NoteEntity n1 = new NoteEntity(null, "T1", "B1", "u1", 100L);
        n1.setId(10L);
        NoteEntity n2 = new NoteEntity(null, "T2", "B2", null, 200L);
        n2.setId(20L);
        when(noteRepository.findAllByOwner(OWNER)).thenReturn(List.of(n1, n2));

        List<NoteDto> result = noteService.listForOwner(OWNER);

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).id);
        // (checks presence/order and id mapping)
        verify(noteRepository).findAllByOwner(OWNER);
    }

    @Test
    void create_assignsOwnerAndCallsRepositoryCreate() {
        NoteDto input = new NoteDto();
        input.title = "Title";
        input.body = "Body";
        input.imageUri = "img-uri";

        when(noteRepository.create(any(NoteEntity.class), org.mockito.ArgumentMatchers.eq(OWNER)))
                .thenAnswer(inv -> {
                    NoteEntity e = inv.getArgument(0);
                    e.setId(99L);
                    return e;
                });

        NoteDto created = noteService.create(OWNER, input);

        assertEquals(99L, created.id);
        assertEquals("Title", created.title);
        assertEquals("Body", created.body);
        assertEquals("img-uri", created.imageUri);
        // createdAt must have been set by the service (value close to now).
        long now = System.currentTimeMillis();
        assert Math.abs(now - created.createdAt) < 5000 : "createdAt should be ~now";
    }

    @Test
    void create_acceptsNullBodyAsEmptyString() {
        NoteDto input = new NoteDto();
        input.title = "T";

        when(noteRepository.create(any(NoteEntity.class), anyLong())).thenAnswer(inv -> {
            NoteEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        NoteDto created = noteService.create(OWNER, input);
        assertEquals("", created.body);
        assertNull(created.imageUri);
    }

    @Test
    void getForOwner_throwsWhenNotFound() {
        when(noteRepository.findByIdAndOwner(5L, OWNER)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> noteService.getForOwner(5L, OWNER));
    }

    @Test
    void getForOwner_returnsDtoWhenOwned() {
        NoteEntity n = new NoteEntity(null, "T", "B", null, 123L);
        n.setId(5L);
        when(noteRepository.findByIdAndOwner(5L, OWNER)).thenReturn(Optional.of(n));

        NoteDto dto = noteService.getForOwner(5L, OWNER);

        assertEquals(5L, dto.id);
        assertEquals("T", dto.title);
        assertEquals(123L, dto.createdAt);
    }

    @Test
    void update_modifiesFieldsButKeepsCreatedAt() {
        NoteEntity existing = new NoteEntity(null, "OldT", "OldB", "oldImg", 999L);
        existing.setId(7L);
        when(noteRepository.findByIdAndOwner(7L, OWNER)).thenReturn(Optional.of(existing));
        when(noteRepository.save(existing)).thenReturn(existing);

        NoteDto input = new NoteDto();
        input.title = "NewT";
        input.body = "NewB";
        input.imageUri = null;

        NoteDto updated = noteService.update(7L, OWNER, input);

        assertEquals("NewT", updated.title);
        assertEquals("NewB", updated.body);
        assertNull(updated.imageUri);
        assertEquals(999L, updated.createdAt, "createdAt must be preserved on update");
    }

    @Test
    void update_throwsWhenNotOwned() {
        when(noteRepository.findByIdAndOwner(7L, OWNER)).thenReturn(Optional.empty());

        NoteDto input = new NoteDto();
        input.title = "X";

        assertThrows(NoteNotFoundException.class, () -> noteService.update(7L, OWNER, input));
        verify(noteRepository, never()).save(any(NoteEntity.class));
    }

    @Test
    void delete_throwsWhenNotDeleted() {
        when(noteRepository.deleteByIdAndOwner(7L, OWNER)).thenReturn(false);

        assertThrows(NoteNotFoundException.class, () -> noteService.delete(7L, OWNER));
    }

    @Test
    void delete_succeedsWhenRepositoryReturnsTrue() {
        when(noteRepository.deleteByIdAndOwner(7L, OWNER)).thenReturn(true);

        noteService.delete(7L, OWNER); // does not throw

        verify(noteRepository).deleteByIdAndOwner(7L, OWNER);
    }
}
