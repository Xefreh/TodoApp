package fr.xefreh.todoapp.backend.service;

import fr.xefreh.todoapp.backend.dto.NoteDto;
import fr.xefreh.todoapp.backend.model.NoteEntity;
import fr.xefreh.todoapp.backend.repository.NoteRepository;
import java.util.List;

/**
 * {@link NoteService} implementation built via dependency injection.
 *
 * The only dependency ({@link NoteRepository}) is an interface: the class is therefore
 * fully testable with a Mockito mock (see NoteServiceTest).
 */
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    public NoteServiceImpl(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public List<NoteDto> listForOwner(long ownerId) {
        return noteRepository.findAllByOwner(ownerId).stream()
                .map(NoteServiceImpl::toDto)
                .toList();
    }

    @Override
    public NoteDto getForOwner(long id, long ownerId) {
        return noteRepository.findByIdAndOwner(id, ownerId)
                .map(NoteServiceImpl::toDto)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }

    @Override
    public NoteDto create(long ownerId, NoteDto input) {
        NoteEntity entity = new NoteEntity(
                null,
                input.title,
                input.body == null ? "" : input.body,
                input.imageUri,
                System.currentTimeMillis());
        return toDto(noteRepository.create(entity, ownerId));
    }

    @Override
    public NoteDto update(long id, long ownerId, NoteDto input) {
        NoteEntity entity = noteRepository.findByIdAndOwner(id, ownerId)
                .orElseThrow(() -> new NoteNotFoundException(id));
        entity.setTitle(input.title);
        entity.setBody(input.body == null ? "" : input.body);
        entity.setImageUri(input.imageUri);
        return toDto(noteRepository.save(entity));
    }

    @Override
    public void delete(long id, long ownerId) {
        boolean deleted = noteRepository.deleteByIdAndOwner(id, ownerId);
        if (!deleted) {
            throw new NoteNotFoundException(id);
        }
    }

    /** Maps a persisted entity to the DTO exposed by the API. */
    private static NoteDto toDto(NoteEntity entity) {
        return new NoteDto(
                entity.getId(),
                entity.getTitle(),
                entity.getBody(),
                entity.getImageUri(),
                entity.getCreatedAt());
    }
}
