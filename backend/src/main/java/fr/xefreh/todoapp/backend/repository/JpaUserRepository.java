package fr.xefreh.todoapp.backend.repository;

import fr.xefreh.todoapp.backend.config.JpaConfig;
import fr.xefreh.todoapp.backend.model.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

/**
 * JPA implementation of {@link UserRepository}. Each method opens a short transaction
 * on the shared {@link EntityManager}.
 */
public class JpaUserRepository implements UserRepository {

    @Override
    public UserEntity findByUsername(String username) {
        EntityManager em = JpaConfig.entityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT u FROM UserEntity u WHERE u.username = :username", UserEntity.class)
                    .setParameter("username", username)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public UserEntity save(UserEntity user) {
        EntityManager em = JpaConfig.entityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        try {
            UserEntity merged = em.merge(user);
            em.getTransaction().commit();
            return merged;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
