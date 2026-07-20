package fr.xefreh.todoapp.backend.repository;

import fr.xefreh.todoapp.backend.config.JpaConfig;
import fr.xefreh.todoapp.backend.model.NoteEntity;
import fr.xefreh.todoapp.backend.model.UserEntity;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of {@link NoteRepository}. All queries filter by the owner's id
 * to guarantee that a user can only access their own notes.
 */
public class JpaNoteRepository implements NoteRepository {

    @Override
    public List<NoteEntity> findAllByOwner(Long ownerId) {
        EntityManager em = JpaConfig.entityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT n FROM NoteEntity n WHERE n.owner.id = :ownerId ORDER BY n.createdAt DESC",
                            NoteEntity.class)
                    .setParameter("ownerId", ownerId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<NoteEntity> findByIdAndOwner(Long id, Long ownerId) {
        EntityManager em = JpaConfig.entityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT n FROM NoteEntity n WHERE n.id = :id AND n.owner.id = :ownerId",
                            NoteEntity.class)
                    .setParameter("id", id)
                    .setParameter("ownerId", ownerId)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }

    @Override
    public NoteEntity create(NoteEntity note, Long ownerId) {
        EntityManager em = JpaConfig.entityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        try {
            // Resolves the owner reference within the same transaction: getReference does
            // not issue a select but validates the FK existence on flush/commit.
            UserEntity owner = em.getReference(UserEntity.class, ownerId);
            note.setOwner(owner);
            em.persist(note);
            em.getTransaction().commit();
            return note;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public NoteEntity save(NoteEntity note) {
        EntityManager em = JpaConfig.entityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        try {
            NoteEntity merged = em.merge(note);
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

    @Override
    public boolean deleteByIdAndOwner(Long id, Long ownerId) {
        EntityManager em = JpaConfig.entityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        try {
            int deleted = em.createQuery(
                            "DELETE FROM NoteEntity n WHERE n.id = :id AND n.owner.id = :ownerId")
                    .setParameter("id", id)
                    .setParameter("ownerId", ownerId)
                    .executeUpdate();
            em.getTransaction().commit();
            return deleted > 0;
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
