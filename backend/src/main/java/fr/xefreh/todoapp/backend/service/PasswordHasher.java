package fr.xefreh.todoapp.backend.service;

/**
 * Password hashing and verification. Interface kept separate from the implementation
 * (argon2id) so it can be mocked in the service unit tests.
 */
public interface PasswordHasher {

    /** Computes the hash of a plain-text password. */
    String hash(String plainPassword);

    /** Verifies that a plain-text password matches a previously computed hash. */
    boolean verify(String plainPassword, String hashedPassword);

    /**
     * A valid but meaningless hash, used to run a full verification when the login username
     * does not exist: keeps the response time constant and avoids leaking whether the
     * account exists (user enumeration via timing).
     */
    String dummyHash();
}
