package pl.agh.lab.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.agh.lab.model.Stable;
import pl.agh.lab.repo.StableRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StableRepository stableRepository;


    @Test
    void testGetAllStables() throws Exception {
        stableRepository.save(new Stable("Existing Stable", 20));

        mockMvc.perform(get("/api/stable"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].stableName", not(emptyOrNullString())));
    }

    @Test
    void testGetHorsesInStable_Success() throws Exception {
        Stable saved = stableRepository.save(new Stable("Target Stable", 15));

        mockMvc.perform(get("/api/stable/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(any(Iterable.class))));
    }

    @Test
    void testGetHorsesInStable_NotFound() throws Exception {
        mockMvc.perform(get("/api/stable/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddStable_Success() throws Exception {
        String json = """
            {
                "name": "New Test Stable",
                "capacity": 50
            }
        """;

        mockMvc.perform(post("/api/stable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stableName", is("New Test Stable")));
    }

    @Test
    void testAddStable_Invalid() throws Exception {
        String json = """
            {
                "name": "Bad Stable",
                "capacity": -10
            }
        """;

        mockMvc.perform(post("/api/stable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteStable_Success() throws Exception {
        Stable saved = stableRepository.save(new Stable("To Delete", 5));

        mockMvc.perform(delete("/api/stable/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/stable/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddHorse_Success() throws Exception {
        Stable stable = stableRepository.save(new Stable("Horse Stable", 10));

        String horseJson = String.format("""
            {
                "stableId": %d,
                "name": "Spirit",
                "breed": "Mustang",
                "type": "GORACOKRWISTY",
                "status": "ZDROWY",
                "age": 4,
                "price": 5000.0,
                "weightKg": 450.0,
                "heightCm": 160.0,
                "microchipId": "CHIP-123"
            }
        """, stable.getId());

        mockMvc.perform(post("/api/horse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(horseJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Spirit")));
    }

    @Test
    void testAddHorse_StableNotFound() throws Exception {
        String horseJson = """
            {
                "stableId": 9999,
                "name": "Ghost",
                "breed": "Unknown",
                "type": "ZIMNOKRWISTY",
                "status": "ZDROWY",
                "age": 5,
                "price": 1000.0,
                "weightKg": 600.0,
                "heightCm": 170.0,
                "microchipId": "CHIP-000"
            }
        """;

        mockMvc.perform(post("/api/horse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(horseJson))
                .andExpect(status().isNotFound());
    }
}