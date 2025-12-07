package pl.agh.lab.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.agh.lab.model.Stable;

public interface StableRepository extends JpaRepository<Stable, Long> {

    // Spring Data сам сгенерирует реализацию по имени метода
    boolean existsByStableName(String stableName);

    // Подсчет общей стоимости всех лошадей (пример JPQL)
    @Query("SELECT COALESCE(SUM(h.price), 0.0) FROM Horse h")
    double totalHerdValue();
}