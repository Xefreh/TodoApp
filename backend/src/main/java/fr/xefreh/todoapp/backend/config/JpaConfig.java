package fr.xefreh.todoapp.backend.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Singleton factory for the JPA {@link EntityManagerFactory} (persistence unit {@code todobackend}).
 *
 * Centralizes creation of the EMF so that repositories can all share the same instance.
 * The persistence unit and the H2 database are configured in {@code META-INF/persistence.xml}.
 */
public final class JpaConfig {

    private static volatile EntityManagerFactory emf;

    private JpaConfig() {
    }

    public static EntityManagerFactory entityManagerFactory() {
        EntityManagerFactory local = emf;
        if (local == null) {
            synchronized (JpaConfig.class) {
                local = emf;
                if (local == null) {
                    local = Persistence.createEntityManagerFactory("todobackend");
                    emf = local;
                }
            }
        }
        return local;
    }

    /** Closes the EMF — call on server shutdown. */
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
