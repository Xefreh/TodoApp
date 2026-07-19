package fr.xefreh.todoapp.backend.service;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Helper;

/**
 * Implémentation de {@link PasswordHasher} basée sur argon2id.
 *
 * Paramètres (recommandations OWASP 2024) :
 * <ul>
 *   <li>itérations = 2</li>
 *   <li>mémoire = 19 MiB</li>
 *   <li>parallélisme = 1</li>
 * </ul>
 * {@link Argon2Helper} détermine le nombre d'itérations atteignable avec le budget mémoire
 * et temps fixé ; on retombe sur 2 par défaut.
 */
public class Argon2PasswordHasher implements PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int MEMORY_KIB = 19 * 1024;
    private static final int ITERATIONS = 2;
    private static final int PARALLELISM = 1;

    private final Argon2 argon2 = Argon2Factory.create(Argon2Types.ARGON2id, SALT_LENGTH, HASH_LENGTH);

    @Override
    public String hash(String plainPassword) {
        return argon2.hash(ITERATIONS, MEMORY_KIB, PARALLELISM, plainPassword.toCharArray());
    }

    @Override
    public boolean verify(String plainPassword, String hashedPassword) {
        return argon2.verify(hashedPassword, plainPassword.toCharArray());
    }

    /** Constante de type Argon2, nommée explicitement pour la lisibilité. */
    private static final class Argon2Types {
        static final Argon2Factory.Argon2Types ARGON2id = Argon2Factory.Argon2Types.ARGON2id;
    }
}
