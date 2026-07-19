package fr.xefreh.todoapp.backend.repository;

import fr.xefreh.todoapp.backend.model.UserEntity;

/**
 * Accès aux utilisateurs. Interface volontairement isolée de l'implémentation JPA
 * afin d'être mockable dans les tests unitaires du service.
 */
public interface UserRepository {

    /** Trouve un utilisateur par son nom, ou {@code null} s'il n'existe pas. */
    UserEntity findByUsername(String username);

    /** Renvoie une référence proxy sur l'utilisateur d'identifiant donné (sans select complet). */
    UserEntity getReference(Long id);

    /** Persiste (crée ou met à jour) un utilisateur. */
    UserEntity save(UserEntity user);
}
