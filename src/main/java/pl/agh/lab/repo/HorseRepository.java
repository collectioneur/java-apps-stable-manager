package pl.agh.lab.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import pl.agh.lab.model.Horse;
import pl.agh.lab.model.HorseCondition;
import pl.agh.lab.model.Stable;

import java.util.List;

public class HorseRepository {

    private final EntityManagerFactory emf;

    public HorseRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }

    public List<Horse> findByStable(Stable stable) {
        EntityManager em = em();
        try {
            TypedQuery<Horse> q = em.createQuery(
                    "SELECT h FROM Horse h WHERE h.stable.id = :sid",
                    Horse.class
            );
            q.setParameter("sid", stable.getId());
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public long countByStable(Stable stable) {
        EntityManager em = em();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(h) FROM Horse h WHERE h.stable.id = :sid",
                            Long.class
                    ).setParameter("sid", stable.getId())
                    .getSingleResult();
            return count != null ? count : 0L;
        } finally {
            em.close();
        }
    }

    public boolean existsDuplicate(Stable stable, String name, String breed, int age) {
        EntityManager em = em();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(h) FROM Horse h " +
                                    "WHERE h.stable.id = :sid " +
                                    "AND LOWER(h.name) = LOWER(:name) " +
                                    "AND LOWER(h.breed) = LOWER(:breed) " +
                                    "AND h.age = :age",
                            Long.class
                    )
                    .setParameter("sid", stable.getId())
                    .setParameter("name", name)
                    .setParameter("breed", breed)
                    .setParameter("age", age)
                    .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    public List<Horse> filter(Stable stable, String fragment, HorseCondition stateFilter) {
        EntityManager em = em();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT h FROM Horse h WHERE h.stable.id = :sid"
            );
            if (fragment != null && !fragment.isBlank()) {
                jpql.append(" AND (LOWER(h.name) LIKE :frag OR LOWER(h.breed) LIKE :frag)");
            }
            if (stateFilter != null) {
                jpql.append(" AND h.status = :status");
            }
            jpql.append(" ORDER BY LOWER(h.name), LOWER(h.breed), h.age");

            TypedQuery<Horse> q = em.createQuery(jpql.toString(), Horse.class);
            q.setParameter("sid", stable.getId());

            if (fragment != null && !fragment.isBlank()) {
                q.setParameter("frag", "%" + fragment.toLowerCase() + "%");
            }
            if (stateFilter != null) {
                q.setParameter("status", stateFilter);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Horse> sortByName(Stable stable) {
        EntityManager em = em();
        try {
            TypedQuery<Horse> q = em.createQuery(
                    "SELECT h FROM Horse h WHERE h.stable.id = :sid " +
                            "ORDER BY LOWER(h.name), LOWER(h.breed), h.age",
                    Horse.class
            );
            q.setParameter("sid", stable.getId());
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Horse> sortByPrice(Stable stable) {
        EntityManager em = em();
        try {
            TypedQuery<Horse> q = em.createQuery(
                    "SELECT h FROM Horse h WHERE h.stable.id = :sid ORDER BY h.price",
                    Horse.class
            );
            q.setParameter("sid", stable.getId());
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Horse save(Horse horse) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            Horse merged = em.merge(horse);
            em.getTransaction().commit();
            return merged;
        } finally {
            em.close();
        }
    }

    public boolean delete(Horse horse) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            Horse managed = em.find(Horse.class, horse.getId());
            if (managed == null) {
                em.getTransaction().commit();
                return false;
            }
            em.remove(managed);
            em.getTransaction().commit();
            return true;
        } finally {
            em.close();
        }
    }
}
