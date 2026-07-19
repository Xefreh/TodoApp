package fr.xefreh.todoapp.backend.service;

import fr.xefreh.todoapp.backend.dto.NoteDto;
import java.util.List;

/**
 * Logique métier des notes, indexée sur l'identifiant du propriétaire authentifié.
 * Interface isolée de l'implémentation ({@link NoteServiceImpl}) afin d'être testée
 * avec un {@code NoteRepository} mocké.
 */
public interface NoteService {

    /** Toutes les notes du propriétaire, de la plus récente à la plus ancienne. */
    List<NoteDto> listForOwner(long ownerId);

    /** Une note du propriétaire. Lève {@link NoteNotFoundException} si absente. */
    NoteDto getForOwner(long id, long ownerId);

    /** Crée une note : assigne le propriétaire et le createdAt courant. */
    NoteDto create(long ownerId, NoteDto input);

    /** Met à jour une note existante du propriétaire. Lève {@link NoteNotFoundException} si absente. */
    NoteDto update(long id, long ownerId, NoteDto input);

    /** Supprime une note du propriétaire. Lève {@link NoteNotFoundException} si absente. */
    void delete(long id, long ownerId);
}
