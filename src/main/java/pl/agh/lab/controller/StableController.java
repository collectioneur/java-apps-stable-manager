package pl.agh.lab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.agh.lab.io.StableCsvUtil;
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

    // 1. POST /api/horse - добавляет коня (нужно передать ID конюшни в теле или параметре)
    @PostMapping("/horse")
    public ResponseEntity<?> addHorse(@RequestBody HorseDTO dto) {
        try {
            // Преобразуем DTO в Entity
            Horse horse = new Horse(dto.name, dto.breed, HorseType.valueOf(dto.type),
                    HorseCondition.valueOf(dto.status), dto.age, dto.price,
                    dto.weightKg, dto.heightCm, dto.microchipId, null);

            Horse created = service.addHorse(dto.stableId, horse);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. DELETE /api/horse/:id
    @DeleteMapping("/horse/{id}")
    public ResponseEntity<?> deleteHorse(@PathVariable Long id) {
        try {
            service.removeHorse(id);
            return ResponseEntity.noContent().build();
        } catch (HorseOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 3. GET /api/horse/rating/:id - средняя оценка
    @GetMapping("/horse/rating/{id}")
    public ResponseEntity<?> getHorseAvgRating(@PathVariable Long id) {
        Double avg = service.getAverageRatingForHorse(id);
        if (avg == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("averageRating", avg));
    }

    // 4. POST /api/horse/rating - добавить оценку
    @PostMapping("/horse/rating")
    public ResponseEntity<?> addRating(@RequestBody RatingDTO dto) {
        try {
            service.addRatingToHorse(dto.horseId, dto.value, dto.description);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. GET /api/stable - все конюшни
    @GetMapping("/stable")
    public List<Stable> getAllStables() {
        return service.getAllStables();
    }

    // 6. GET /api/stable/:id - все кони в конюшне
    @GetMapping("/stable/{id}")
    public ResponseEntity<?> getHorsesInStable(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getHorses(id));
        } catch (StableOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 7. GET /api/stable/:id/csv - CSV файл
    @GetMapping(value = "/stable/{id}/csv", produces = "text/csv")
    public ResponseEntity<?> getStableCsv(@PathVariable Long id) {
        try {
            Stable stable = service.getStable(id)
                    .orElseThrow(() -> new StableOperationException("Stable not found"));

            // Используем утилиту, но пишем в поток в памяти
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // NB: Нужно немного изменить StableCsvUtil чтобы он принимал OutputStream,
            // или временно сохранить в файл. Ниже пример если мы адаптировали утилиту:
            // StableCsvUtil.exportToStream(stable, service.getHorses(id), out);

            // Простая генерация CSV "на лету" для примера:
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

    // 8. POST /api/stable - новая конюшня
    @PostMapping("/stable")
    public ResponseEntity<?> addStable(@RequestBody StableDTO dto) {
        try {
            Stable s = service.addStable(dto.name, dto.capacity);
            return new ResponseEntity<>(s, HttpStatus.CREATED);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 9. DELETE /api/stable/:id - удалить конюшню
    @DeleteMapping("/stable/{id}")
    public ResponseEntity<?> deleteStable(@PathVariable Long id) {
        try {
            service.removeStable(id);
            return ResponseEntity.noContent().build();
        } catch (StableOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 10. GET /api/stable/:id/fill - заполнение
    @GetMapping("/stable/{id}/fill")
    public ResponseEntity<?> getStableFill(@PathVariable Long id) {
        return service.getStable(id)
                .map(s -> {
                    long count = service.getHorses(id).size(); // Или через countByStable репозитория
                    return ResponseEntity.ok(Map.of(
                            "stableName", s.getStableName(),
                            "current", count,
                            "max", s.getMaxCapacity(),
                            "percentage", (double)count / s.getMaxCapacity()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Вспомогательные DTO классы (можно вынести в отдельные файлы)
    public record HorseDTO(Long stableId, String name, String breed, String type, String status, int age, double price, double weightKg, double heightCm, String microchipId) {}
    public record RatingDTO(Long horseId, int value, String description) {}
    public record StableDTO(String name, int capacity) {}
}