package pl.agh.lab.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import pl.agh.lab.model.Stable;

import java.util.List;
import java.util.Optional;

public class StableRepository {

    private final EntityManagerFactory emf;

    public StableRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }

    public List<Stable> findAll() {
        EntityManager em = em();
        try {
            TypedQuery<Stable> q = em.createQuery(
                    "SELECT s FROM Stable s ORDER BY s.stableName",
                    Stable.class
            );
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Stable> findByName(String name) {
        EntityManager em = em();
        try {
            TypedQuery<Stable> q = em.createQuery(
                    "SELECT s FROM Stable s WHERE LOWER(s.stableName) = LOWER(:name)",
                    Stable.class
            );
            q.setParameter("name", name);
            List<Stable> result = q.getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    public Stable save(Stable stable) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            Stable merged = em.merge(stable);
            em.getTransaction().commit();
            return merged;
        } finally {
            em.close();
        }
    }

    public boolean deleteByName(String name) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            TypedQuery<Stable> q = em.createQuery(
                    "SELECT s FROM Stable s WHERE LOWER(s.stableName) = LOWER(:name)",
                    Stable.class
            );
            q.setParameter("name", name);
            List<Stable> result = q.getResultList();
            if (result.isEmpty()) {
                em.getTransaction().commit();
                return false;
            }
            for (Stable s : result) {
                Stable managed = em.contains(s) ? s : em.merge(s);
                em.remove(managed);
            }
            em.getTransaction().commit();
            return true;
        } finally {
            em.close();
        }
    }

    public double totalHerdValue() {
        EntityManager em = em();
        try {
            Double sum = em.createQuery(
                    "SELECT COALESCE(SUM(h.price), 0.0) FROM Horse h",
                    Double.class
            ).getSingleResult();
            return sum != null ? sum : 0.0;
        } finally {
            em.close();
        }
    }
}
