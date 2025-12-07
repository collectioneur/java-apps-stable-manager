package pl.agh.lab.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.agh.lab.model.*;
import pl.agh.lab.repo.*;

import java.util.*;

@Service
@Transactional
public class StableService {

    private final StableRepository stableRepo;
    private final HorseRepository horseRepo;
    private final RatingRepository ratingRepo;

    // Spring автоматически внедрит репозитории через конструктор
    public StableService(StableRepository stableRepo, HorseRepository horseRepo, RatingRepository ratingRepo) {
        this.stableRepo = stableRepo;
        this.horseRepo = horseRepo;
        this.ratingRepo = ratingRepo;
    }

    public List<HorseRatingStat> getHorseRatingStatsForStable(Stable stable) {
        // ID должен быть не null
        if (stable.getId() == null) return new ArrayList<>();
        return ratingRepo.findStatsForStable(stable.getId());
    }

    // Метод для задания 1.3
    public Double getAverageRatingForHorse(Long horseId) {
        return ratingRepo.getAverageRatingForHorse(horseId);
    }

    public List<Stable> getAllStables() {
        return stableRepo.findAll();
    }

    public Optional<Stable> getStable(Long id) {
        return stableRepo.findById(id);
    }

    public Optional<Horse> getHorse(Long id) {
        return horseRepo.findById(id);
    }

    public Stable addStable(String name, int capacity) throws ValidationException {
        name = name == null ? "" : name.trim();
        if (name.isEmpty()) throw new ValidationException("Stable name is required");
        if (capacity <= 0) throw new ValidationException("Stable capacity must be > 0");
        if (stableRepo.existsByStableName(name)) throw new ValidationException("Stable '" + name + "' already exists");

        return stableRepo.save(new Stable(name, capacity));
    }

    public void removeStable(Long id) throws StableOperationException {
        if (!stableRepo.existsById(id)) {
            throw new StableOperationException("Stable not found");
        }
        stableRepo.deleteById(id);
    }

    public List<Horse> getHorses(Long stableId) throws StableOperationException {
        Stable stable = stableRepo.findById(stableId)
                .orElseThrow(() -> new StableOperationException("Stable not found"));
        return horseRepo.findByStable(stable);
    }

    public Horse addHorse(Long stableId, Horse horseData) throws ValidationException, StableOperationException, HorseOperationException {
        Stable stable = stableRepo.findById(stableId)
                .orElseThrow(() -> new StableOperationException("Stable not found"));

        // Валидация (упрощено, предполагаем что horseData содержит нужные поля)
        if (horseRepo.existsDuplicate(stable, horseData.getName(), horseData.getBreed(), horseData.getAge())) {
            throw new HorseOperationException("Horse already exists");
        }
        if (horseRepo.countByStable(stable) >= stable.getMaxCapacity()) {
            throw new StableOperationException("Stable is full");
        }

        // Привязываем к конюшне
        stable.addHorse(horseData);
        // Важно: в JPA сущность Horse владеет связью, сохраняем horse
        return horseRepo.save(horseData);
    }

    public void removeHorse(Long horseId) throws HorseOperationException {
        if (!horseRepo.existsById(horseId)) {
            throw new HorseOperationException("Horse not found");
        }
        horseRepo.deleteById(horseId);
    }

    public Rating addRatingToHorse(Long horseId, int value, String description) throws ValidationException, HorseOperationException {
        Horse horse = horseRepo.findById(horseId)
                .orElseThrow(() -> new HorseOperationException("Horse not found"));

        Rating rating = new Rating(value, horse, new Date(), description);
        return ratingRepo.save(rating);
    }

    // Остальные методы (сортировка и т.д.) можно адаптировать аналогично
}