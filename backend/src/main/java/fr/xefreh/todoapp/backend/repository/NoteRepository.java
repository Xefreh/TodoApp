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

    /**
     * Crée une note appartenant à l'utilisateur {@code ownerId}. Les champs de {@code note}
     * (title, body, imageUri, createdAt) sont recopiés ; le owner est résolu dans la même
     * transaction. L'id de {@code note} est ignoré (auto-généré).
     */
    NoteEntity create(NoteEntity note, Long ownerId);

    /** Met à jour une note existante (le owner et l'id de {@code note} doivent être positionnés). */
    NoteEntity save(NoteEntity note);

    /** Supprime une note par son identifiant si elle appartient au propriétaire. Renvoie vrai si supprimée. */
    boolean deleteByIdAndOwner(Long id, Long ownerId);
}
