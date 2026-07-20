package fr.xefreh.todoapp.backend.repository;

import fr.xefreh.todoapp.backend.model.UserEntity;

/**
 * Access to users. Interface intentionally kept separate from the JPA implementation
 * so it can be mocked in the service unit tests.
 */
public interface UserRepository {

    /** Finds a user by name, or {@code null} if it does not exist. */
    UserEntity findByUsername(String username);

    /** Returns a proxy reference to the user with the given id (without a full select). */
    UserEntity getReference(Long id);

    /** Persists (creates or updates) a user. */
    UserEntity save(UserEntity user);
}
