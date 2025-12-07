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

    public StableService(StableRepository stableRepo, HorseRepository horseRepo, RatingRepository ratingRepo) {
        this.stableRepo = stableRepo;
        this.horseRepo = horseRepo;
        this.ratingRepo = ratingRepo;
    }

    // --- МЕТОДЫ ЧТЕНИЯ ---

    public List<Stable> getAllStables() {
        return stableRepo.findAll();
    }

    public Optional<Stable> getStable(Long id) {
        return stableRepo.findById(id);
    }

    public Optional<Horse> getHorse(Long id) {
        return horseRepo.findById(id);
    }

    // Метод для REST API
    public List<Horse> getHorses(Long stableId) throws StableOperationException {
        Stable stable = stableRepo.findById(stableId)
                .orElseThrow(() -> new StableOperationException("Stable not found"));
        return horseRepo.findByStable(stable);
    }

    // Метод для Swing (перегрузка)
    public List<Horse> getHorses(Stable stable) {
        if (stable == null || stable.getId() == null) return new ArrayList<>();
        return horseRepo.findByStable(stable);
    }

    public List<HorseRatingStat> getHorseRatingStatsForStable(Stable stable) {
        if (stable == null || stable.getId() == null) return new ArrayList<>();
        return ratingRepo.findStatsForStable(stable.getId());
    }

    public Double getAverageRatingForHorse(Long horseId) {
        return ratingRepo.getAverageRatingForHorse(horseId);
    }

    public List<Horse> filterHorses(Stable stable, String nameFragment, HorseCondition stateFilter) {
        if (stable == null || stable.getId() == null) return new ArrayList<>();
        return horseRepo.filter(stable, nameFragment, stateFilter);
    }

    public List<Stable> sortStablesByCurrentLoad() {
        List<Stable> all = getAllStables();
        all.sort(Comparator.comparingDouble(s -> {
            long count = horseRepo.countByStable(s);
            if (s.getMaxCapacity() == 0) return 0.0;
            return (double) count / s.getMaxCapacity();
        }));
        return all;
    }

    // --- МЕТОДЫ ИЗМЕНЕНИЯ (STABLES) ---

    public Stable addStable(String name, int capacity) throws ValidationException {
        name = name == null ? "" : name.trim();
        if (name.isEmpty()) throw new ValidationException("Stable name is required");
        if (capacity <= 0) throw new ValidationException("Stable capacity must be > 0");
        if (stableRepo.existsByStableName(name)) throw new ValidationException("Stable '" + name + "' already exists");

        return stableRepo.save(new Stable(name, capacity));
    }

    // Метод для REST API
    public void removeStable(Long id) throws StableOperationException {
        if (!stableRepo.existsById(id)) {
            throw new StableOperationException("Stable not found");
        }
        stableRepo.deleteById(id);
    }

    // Метод для Swing (перегрузка)
    public void removeStable(Stable stable) throws StableOperationException {
        if (stable == null || stable.getId() == null) throw new StableOperationException("Invalid stable");
        removeStable(stable.getId());
    }

    // --- МЕТОДЫ ИЗМЕНЕНИЯ (HORSES) ---

    // Метод для Swing (полный набор параметров)
    public Horse addHorse(Stable stable, String name, String breed, HorseType type, HorseCondition status, int age, double price, double weightKg, double heightCm, String microchipId, Date acquisitionDate)
            throws ValidationException, StableOperationException, HorseOperationException {

        if (stable == null || stable.getId() == null) throw new StableOperationException("Stable not managed");

        // Простая валидация
        if (name == null || name.isBlank()) throw new ValidationException("Name is required");

        if (horseRepo.existsDuplicate(stable, name, breed, age)) {
            throw new HorseOperationException("Horse already exists");
        }
        if (horseRepo.countByStable(stable) >= stable.getMaxCapacity()) {
            throw new StableOperationException("Stable is full");
        }

        Horse horse = new Horse(name, breed, type, status, age, price, weightKg, heightCm, microchipId, acquisitionDate);
        stable.addHorse(horse); // Связываем объекты
        return horseRepo.save(horse);
    }

    // Метод для REST API (через DTO/объект)
    public Horse addHorse(Long stableId, Horse horseData) throws ValidationException, StableOperationException, HorseOperationException {
        Stable stable = stableRepo.findById(stableId)
                .orElseThrow(() -> new StableOperationException("Stable not found"));

        return addHorse(stable, horseData.getName(), horseData.getBreed(), horseData.getType(),
                horseData.getStatus(), horseData.getAge(), horseData.getPrice(),
                horseData.getWeightKg(), horseData.getHeightCm(), horseData.getMicrochipId(),
                horseData.getAcquisitionDate());
    }

    // Метод для REST API
    public void removeHorse(Long horseId) throws HorseOperationException {
        if (!horseRepo.existsById(horseId)) throw new HorseOperationException("Horse not found");
        horseRepo.deleteById(horseId);
    }

    // Метод для Swing
    public void removeHorse(Stable stable, Horse horse) throws StableOperationException, HorseOperationException {
        if (horse == null || horse.getId() == null) throw new HorseOperationException("Horse not found");
        removeHorse(horse.getId());
    }

    // --- МЕТОДЫ ИЗМЕНЕНИЯ (RATINGS) ---

    // Метод для Swing
    public Rating addRatingToHorse(Horse horse, int value, String description) throws ValidationException, HorseOperationException {
        if (horse == null || horse.getId() == null) throw new HorseOperationException("Horse not found");

        if (value < 0 || value > 5) throw new ValidationException("Rating must be 0-5");

        Rating rating = new Rating(value, horse, new Date(), description);
        return ratingRepo.save(rating);
    }

    // Метод для REST API
    public Rating addRatingToHorse(Long horseId, int value, String description) throws ValidationException, HorseOperationException {
        Horse horse = horseRepo.findById(horseId)
                .orElseThrow(() -> new HorseOperationException("Horse not found"));
        return addRatingToHorse(horse, value, description);
    }
}