package pl.agh.lab.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.agh.lab.model.Horse;
import pl.agh.lab.model.HorseCondition;
import pl.agh.lab.model.Stable;

import java.util.List;

public interface HorseRepository extends JpaRepository<Horse, Long> {

    List<Horse> findByStable(Stable stable);

    long countByStable(Stable stable);

    @Query("SELECT COUNT(h) > 0 FROM Horse h WHERE h.stable = :stable AND LOWER(h.name) = LOWER(:name) AND LOWER(h.breed) = LOWER(:breed) AND h.age = :age")
    boolean existsDuplicate(@Param("stable") Stable stable, @Param("name") String name, @Param("breed") String breed, @Param("age") int age);

    @Query("SELECT h FROM Horse h WHERE h.stable = :stable " +
            "AND (:fragment IS NULL OR LOWER(h.name) LIKE %:fragment% OR LOWER(h.breed) LIKE %:fragment%) " +
            "AND (:status IS NULL OR h.status = :status) " +
            "ORDER BY h.name, h.breed, h.age")
    List<Horse> filter(@Param("stable") Stable stable, @Param("fragment") String fragment, @Param("status") HorseCondition status);
}