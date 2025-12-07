package pl.agh.lab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.agh.lab.model.*;
import pl.agh.lab.service.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StableController {

    private final StableService service;

    public StableController(StableService service) {
        this.service = service;
    }

    @PostMapping("/horse")
    public ResponseEntity<?> addHorse(@RequestBody HorseDTO dto) {
        try {
            Horse horse = new Horse(dto.name, dto.breed, HorseType.valueOf(dto.type),
                    HorseCondition.valueOf(dto.status), dto.age, dto.price,
                    dto.weightKg, dto.heightCm, dto.microchipId, null);

            Horse created = service.addHorse(dto.stableId, horse);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (StableOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ValidationException | HorseOperationException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/horse/{id}")
    public ResponseEntity<?> deleteHorse(@PathVariable Long id) {
        try {
            service.removeHorse(id);
            return ResponseEntity.noContent().build();
        } catch (HorseOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/horse/rating/{id}")
    public ResponseEntity<?> getHorseAvgRating(@PathVariable Long id) {
        Double avg = service.getAverageRatingForHorse(id);
        if (avg == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("averageRating", avg));
    }

    @PostMapping("/horse/rating")
    public ResponseEntity<?> addRating(@RequestBody RatingDTO dto) {
        try {
            service.addRatingToHorse(dto.horseId, dto.value, dto.description);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (HorseOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/stable")
    public List<Stable> getAllStables() {
        return service.getAllStables();
    }

    @GetMapping("/stable/{id}")
    public ResponseEntity<?> getHorsesInStable(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getHorses(id));
        } catch (StableOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping(value = "/stable/{id}/csv", produces = "text/csv")
    public ResponseEntity<?> getStableCsv(@PathVariable Long id) {
        try {
            service.getStable(id).orElseThrow(() -> new StableOperationException("Stable not found"));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(out);
            writer.println("name,breed,age,price");
            for(Horse h : service.getHorses(id)) {
                writer.printf("%s,%s,%d,%.2f%n", h.getName(), h.getBreed(), h.getAge(), h.getPrice());
            }
            writer.flush();

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=stable_" + id + ".csv")
                    .body(out.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/stable")
    public ResponseEntity<?> addStable(@RequestBody StableDTO dto) {
        try {
            Stable s = service.addStable(dto.name, dto.capacity);
            return new ResponseEntity<>(s, HttpStatus.CREATED);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/stable/{id}")
    public ResponseEntity<?> deleteStable(@PathVariable Long id) {
        try {
            service.removeStable(id);
            return ResponseEntity.noContent().build();
        } catch (StableOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/stable/{id}/fill")
    public ResponseEntity<?> getStableFill(@PathVariable Long id) {
        return service.getStable(id)
                .map(s -> {
                    long count = 0;
                    try {
                        count = service.getHorses(id).size();
                    } catch (StableOperationException e) {
                        // ignore
                    }
                    return ResponseEntity.ok(Map.of(
                            "stableName", s.getStableName(),
                            "current", count,
                            "max", s.getMaxCapacity(),
                            "percentage", (double)count / s.getMaxCapacity()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public record HorseDTO(Long stableId, String name, String breed, String type, String status, int age, double price, double weightKg, double heightCm, String microchipId) {}
    public record RatingDTO(Long horseId, int value, String description) {}
    public record StableDTO(String name, int capacity) {}
}