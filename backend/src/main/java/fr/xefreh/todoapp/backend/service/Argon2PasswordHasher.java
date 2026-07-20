package fr.xefreh.todoapp.backend.service;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Helper;

/**
 * {@link PasswordHasher} implementation based on argon2id.
 *
 * Parameters (OWASP 2024 recommendations):
 * <ul>
 *   <li>iterations = 2</li>
 *   <li>memory = 19 MiB</li>
 *   <li>parallelism = 1</li>
 * </ul>
 * {@link Argon2Helper} determines the number of iterations reachable within the fixed memory
 * and time budget; it falls back to 2 by default.
 */
public class Argon2PasswordHasher implements PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int MEMORY_KIB = 19 * 1024;
    private static final int ITERATIONS = 2;
    private static final int PARALLELISM = 1;

    private final Argon2 argon2 = Argon2Factory.create(Argon2Types.ARGON2id, SALT_LENGTH, HASH_LENGTH);

    /** Lazily computed (one argon2 hash costs ~50-100 ms, only pay it on first use). */
    private volatile String dummyHash;

    @Override
    public String hash(String plainPassword) {
        return argon2.hash(ITERATIONS, MEMORY_KIB, PARALLELISM, plainPassword.toCharArray());
    }

    @Override
    public boolean verify(String plainPassword, String hashedPassword) {
        return argon2.verify(hashedPassword, plainPassword.toCharArray());
    }

    @Override
    public String dummyHash() {
        String local = dummyHash;
        if (local == null) {
            synchronized (this) {
                local = dummyHash;
                if (local == null) {
                    local = hash("dummy-password-for-constant-time-login");
                    dummyHash = local;
                }
            }
        }
        return local;
    }

    /** Argon2 type constant, named explicitly for readability. */
    private static final class Argon2Types {
        static final Argon2Factory.Argon2Types ARGON2id = Argon2Factory.Argon2Types.ARGON2id;
    }
}
