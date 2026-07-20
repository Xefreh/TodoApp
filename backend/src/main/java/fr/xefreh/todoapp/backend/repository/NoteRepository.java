package fr.xefreh.todoapp.backend.repository;

import fr.xefreh.todoapp.backend.model.NoteEntity;
import java.util.List;
import java.util.Optional;

/**
 * Access to notes. Interface kept separate from the JPA implementation so it can be mocked
 * in the service unit tests. All methods are keyed on the owner's id to guarantee per-user
 * data isolation.
 */
public interface NoteRepository {

    /** All notes of an owner, sorted from newest to oldest. */
    List<NoteEntity> findAllByOwner(Long ownerId);

    /** A specific note if it belongs to the owner, otherwise empty. */
    Optional<NoteEntity> findByIdAndOwner(Long id, Long ownerId);

    /**
     * Creates a note owned by user {@code ownerId}. The fields of {@code note}
     * (title, body, imageUri, createdAt) are copied; the owner is resolved within the same
     * transaction. The id of {@code note} is ignored (auto-generated).
     */
    NoteEntity create(NoteEntity note, Long ownerId);

    /** Updates an existing note (the owner and id of {@code note} must be set). */
    NoteEntity save(NoteEntity note);

    /** Deletes a note by its id if it belongs to the owner. Returns true if deleted. */
    boolean deleteByIdAndOwner(Long id, Long ownerId);
}
