package fr.xefreh.todoapp.backend.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Factory singleton de l'{@link EntityManagerFactory} JPA (unité de persistance {@code todobackend}).
 *
 * Centralise la création de l'EMF pour que les repositories puissent tous partager la même
 * instance. L'unité de persistance et la base H2 sont configurées dans
 * {@code META-INF/persistence.xml}.
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

    /** Ferme l'EMF — à appeler à l'arrêt du serveur. */
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
