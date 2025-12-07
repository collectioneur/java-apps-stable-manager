package pl.agh.lab.service;

import jakarta.persistence.EntityManagerFactory;
import pl.agh.lab.model.*;
import pl.agh.lab.repo.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StableService {

    private final StableRepository stableRepo;
    private final HorseRepository horseRepo;
    private final RatingRepository ratingRepo;

    public StableService(EntityManagerFactory emf) {
        this.stableRepo = new StableRepository(emf);
        this.horseRepo = new HorseRepository(emf);
        this.ratingRepo = new RatingRepository(emf);
    }

    public List<HorseRatingStat> getHorseRatingStatsForStable(Stable stable)
            throws StableOperationException {

        Stable dbStable = ensureStableExists(stable);
        return ratingRepo.findStatsForStable(dbStable);
    }

    public List<Stable> getAllStables() {
        return stableRepo.findAll();
    }

    public Stable addStable(String name, int capacity) throws ValidationException {
        name = name == null ? "" : name.trim();

        if (name.isEmpty()) {
            throw new ValidationException("Stable name is required");
        }
        if (capacity <= 0) {
            throw new ValidationException("Stable capacity must be > 0");
        }

        if (stableRepo.findByName(name).isPresent()) {
            throw new ValidationException("Stable '" + name + "' already exists");
        }

        Stable stable = new Stable(name, capacity);
        return stableRepo.save(stable);
    }

    public void removeStable(Stable stable) throws StableOperationException {
        if (stable == null) {
            throw new StableOperationException("Stable must not be null");
        }
        String name = stable.getStableName();
        boolean removed = stableRepo.deleteByName(name);
        if (!removed) {
            throw new StableOperationException("Stable '" + name + "' does not exist");
        }
    }

    public List<Stable> sortStablesByCurrentLoad() {
        List<Stable> all = getAllStables();
        all.sort(Comparator.comparingDouble(stable -> {
            int cur = horseRepo.findByStable(stable).size();
            int max = stable.getMaxCapacity();
            if (max == 0) return 0.0;
            return (double) cur / max;
        }));
        return all;
    }

    public double getTotalHerdValue() {
        return stableRepo.totalHerdValue();
    }


    public List<Horse> getHorses(Stable stable) throws StableOperationException {
        ensureStableExists(stable);
        return new ArrayList<>(horseRepo.findByStable(stable));
    }

    public Horse addHorse(
            Stable stable,
            String name,
            String breed,
            HorseType type,
            HorseCondition status,
            int age,
            double price,
            double weightKg,
            double heightCm,
            String microchipId,
            Date acquisitionDate
    ) throws ValidationException, StableOperationException, HorseOperationException {

        Stable dbStable = ensureStableExists(stable);

        name = safeTrim(name);
        breed = safeTrim(breed);
        microchipId = microchipId == null ? "" : microchipId.trim();

        if (name.isEmpty()) {
            throw new ValidationException("Horse name is required");
        }
        if (breed.isEmpty()) {
            throw new ValidationException("Horse breed is required");
        }
        if (type == null) {
            throw new ValidationException("Horse type is required");
        }
        if (status == null) {
            throw new ValidationException("Horse status is required");
        }
        if (age < 0) {
            throw new ValidationException("Horse age must be >= 0");
        }
        if (price < 0) {
            throw new ValidationException("Horse price must be >= 0");
        }
        if (weightKg <= 0) {
            throw new ValidationException("Horse weight must be > 0");
        }
        if (heightCm <= 0) {
            throw new ValidationException("Horse height must be > 0");
        }

        if (horseRepo.existsDuplicate(dbStable, name, breed, age)) {
            throw new HorseOperationException(String.format(
                    Locale.ROOT,
                    "Horse '%s' (%s, %d years) already exists in stable '%s'",
                    name, breed, age, dbStable.getStableName()
            ));
        }

        long currentSize = horseRepo.countByStable(dbStable);
        if (currentSize >= dbStable.getMaxCapacity()) {
            throw new StableOperationException(String.format(
                    Locale.ROOT,
                    "Stable '%s' is full (%d/%d)",
                    dbStable.getStableName(), currentSize, dbStable.getMaxCapacity()
            ));
        }

        try {
            Horse horse = new Horse(
                    name,
                    breed,
                    type,
                    status,
                    age,
                    price,
                    weightKg,
                    heightCm,
                    microchipId,
                    acquisitionDate
            );
            dbStable.addHorse(horse);
            return horseRepo.save(horse);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid horse data: " + e.getMessage(), e);
        }
    }

    public void removeHorse(Stable stable, Horse horse)
            throws StableOperationException, HorseOperationException {

        ensureStableExists(stable);

        if (horse == null) {
            throw new HorseOperationException("Horse must not be null");
        }

        boolean removed = horseRepo.delete(horse);
        if (!removed) {
            throw new HorseOperationException(String.format(
                    Locale.ROOT,
                    "Horse '%s' not found in stable '%s'",
                    horse.getName(),
                    stable.getStableName()
            ));
        }
    }

    public void changeHorseStatus(Stable stable, Horse horse, HorseCondition newStatus)
            throws StableOperationException, HorseOperationException, ValidationException {

        Stable dbStable = ensureStableExists(stable);

        if (horse == null) {
            throw new HorseOperationException("Horse must not be null");
        }
        if (newStatus == null) {
            throw new ValidationException("Horse status must not be null");
        }

        List<Horse> horses = horseRepo.findByStable(dbStable);
        if (horses.stream().noneMatch(h -> h.getId().equals(horse.getId()))) {
            throw new HorseOperationException("Horse does not belong to this stable");
        }

        horse.setStatus(newStatus);
        horseRepo.save(horse);
    }

    public void changeHorseWeight(Stable stable, Horse horse, double delta)
            throws StableOperationException, HorseOperationException, ValidationException {

        Stable dbStable = ensureStableExists(stable);

        if (horse == null) {
            throw new HorseOperationException("Horse must not be null");
        }

        List<Horse> horses = horseRepo.findByStable(dbStable);
        if (horses.stream().noneMatch(h -> h.getId().equals(horse.getId()))) {
            throw new HorseOperationException("Horse does not belong to this stable");
        }

        try {
            horse.changeWeight(delta);
            horseRepo.save(horse);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid weight change: " + e.getMessage(), e);
        }
    }

    public List<Horse> filterHorses(
            Stable stable,
            String nameFragment,
            HorseCondition stateFilter
    ) throws StableOperationException {

        Stable dbStable = ensureStableExists(stable);
        String fragment = safeTrim(nameFragment);

        return horseRepo.filter(dbStable, fragment, stateFilter);
    }

    public List<Horse> sortHorsesByName(Stable stable) throws StableOperationException {
        Stable dbStable = ensureStableExists(stable);
        return horseRepo.sortByName(dbStable);
    }

    public List<Horse> sortHorsesByPrice(Stable stable) throws StableOperationException {
        Stable dbStable = ensureStableExists(stable);
        return horseRepo.sortByPrice(dbStable);
    }

    private Stable ensureStableExists(Stable stable) throws StableOperationException {
        if (stable == null) {
            throw new StableOperationException("Stable must not be null");
        }
        return stableRepo.findByName(stable.getStableName())
                .orElseThrow(() -> new StableOperationException(
                        "Stable '" + stable.getStableName() + "' is not registered in DB"
                ));
    }

    public Rating addRatingToHorse(Horse horse, int value, String description)
            throws ValidationException, HorseOperationException {

        if (horse == null) {
            throw new HorseOperationException("Horse must not be null");
        }
        if (horse.getId() == null) {
            throw new HorseOperationException("Horse must be persisted before rating");
        }
        if (value < 0 || value > 5) {
            throw new ValidationException("Rating value must be between 0 and 5");
        }

        String desc = (description == null) ? "" : description.trim();


        Rating rating = new Rating(
                value,
                horse,
                new Date(),
                desc
        );

        return ratingRepo.save(rating);
    }


    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
