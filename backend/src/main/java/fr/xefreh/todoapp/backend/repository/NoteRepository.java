package fr.xefreh.todoapp.backend.repository;

import fr.xefreh.todoapp.backend.model.NoteEntity;
import java.util.List;
import java.util.Optional;

/**
 * Accès aux notes. Interface isolée de l'implémentation JPA afin d'être mockable
 * dans les tests unitaires du service. Toutes les méthodes sont indexées sur
 * l'identifiant du propriétaire pour garantir l'isolation des données par utilisateur.
 */
public interface NoteRepository {

    /** Toutes les notes d'un propriétaire, triées de la plus récente à la plus ancienne. */
    List<NoteEntity> findAllByOwner(Long ownerId);

    /** Une note précise si elle appartient au propriétaire, sinon vide. */
    Optional<NoteEntity> findByIdAndOwner(Long id, Long ownerId);

    /** Persiste (crée ou met à jour) une note. */
    NoteEntity save(NoteEntity note);

    /** Supprime une note par son identifiant si elle appartient au propriétaire. Renvoie vrai si supprimée. */
    boolean deleteByIdAndOwner(Long id, Long ownerId);
}
