package pl.agh.lab.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import pl.agh.lab.model.Horse;
import pl.agh.lab.model.Rating;
import pl.agh.lab.model.Stable;
import pl.agh.lab.service.HorseRatingStat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RatingRepository {

    private final EntityManagerFactory emf;

    public RatingRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }

    public List<HorseRatingStat> findStatsForStable(Stable stable) {
        EntityManager em = em();
        try {
            var cb = em.getCriteriaBuilder();

            CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
            Root<Rating> rating = cq.from(Rating.class);

            cq.multiselect(
                    rating.get("horse").get("name"),
                    cb.count(rating),
                    cb.avg(rating.get("value"))
            );

            cq.where(cb.equal(rating.get("horse").get("stable").get("id"), stable.getId()));

            cq.groupBy(rating.get("horse").get("name"));

            List<Object[]> rows = em.createQuery(cq).getResultList();

            List<HorseRatingStat> result = new ArrayList<>();
            for (Object[] row : rows) {
                String horseName = (String) row[0];
                Long count = (Long) row[1];
                Double avg = (Double) row[2];
                result.add(new HorseRatingStat(
                        horseName,
                        count != null ? count : 0L,
                        avg != null ? avg : 0.0
                ));
            }

            return result;
        } finally {
            em.close();
        }
    }


    public Rating save(Rating rating) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            Rating merged = em.merge(rating);
            em.getTransaction().commit();
            return merged;
        } finally {
            em.close();
        }
    }
}
