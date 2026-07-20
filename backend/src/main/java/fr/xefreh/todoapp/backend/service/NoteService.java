package fr.xefreh.todoapp.backend.service;

import fr.xefreh.todoapp.backend.dto.NoteDto;
import java.util.List;

/**
 * Note business logic, keyed on the authenticated owner's id.
 * Interface kept separate from the implementation ({@link NoteServiceImpl}) so it can be
 * tested with a mocked {@code NoteRepository}.
 */
public interface NoteService {

    /** All notes of the owner, from newest to oldest. */
    List<NoteDto> listForOwner(long ownerId);

    /** One of the owner's notes. Throws {@link NoteNotFoundException} if absent. */
    NoteDto getForOwner(long id, long ownerId);

    /** Creates a note: assigns the owner and the current createdAt. */
    NoteDto create(long ownerId, NoteDto input);

    /** Updates an existing note of the owner. Throws {@link NoteNotFoundException} if absent. */
    NoteDto update(long id, long ownerId, NoteDto input);

    /** Deletes a note of the owner. Throws {@link NoteNotFoundException} if absent. */
    void delete(long id, long ownerId);
}
