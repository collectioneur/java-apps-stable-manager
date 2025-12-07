package pl.agh.lab.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.agh.lab.model.Horse;
import pl.agh.lab.model.Rating;
import pl.agh.lab.service.HorseRatingStat;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    @Query("SELECT new pl.agh.lab.service.HorseRatingStat(r.horse.name, COUNT(r), AVG(r.value)) " +
            "FROM Rating r WHERE r.horse.stable.id = :stableId GROUP BY r.horse.name")
    List<HorseRatingStat> findStatsForStable(@Param("stableId") Long stableId);

    // Для задания 1.3: Средняя оценка конкретной лошади
    @Query("SELECT AVG(r.value) FROM Rating r WHERE r.horse.id = :horseId")
    Double getAverageRatingForHorse(@Param("horseId") Long horseId);
}