package pl.agh.lab.controller;

public class StableControllerTest {
    package pl.agh.lab.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// Импорты для построения запросов и проверок
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

    // Говорим Spring Boot поднять полный контекст приложения для теста
    @SpringBootTest
// Автоматически настраиваем MockMvc (инструмент для имитации HTTP-запросов)
    @AutoConfigureMockMvc
    class StableControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void testGetAllStables() throws Exception {
            // Выполняем GET запрос на /api/stable
            mockMvc.perform(get("/api/stable"))
                    // Ожидаем, что статус ответа будет 200 (OK)
                    .andExpect(status().isOk())
                    // Ожидаем, что ответ будет в формате JSON
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    // Ожидаем, что вернется массив (даже пустой)
                    .andExpect(jsonPath("$", is(any(Iterable.class))));
        }

        @Test
        void testAddStable() throws Exception {
            // Подготавливаем JSON для новой конюшни
            String newStableJson = """
            {
                "name": "Test Stable",
                "capacity": 10
            }
        """;

            // Выполняем POST запрос на /api/stable
            mockMvc.perform(post("/api/stable")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newStableJson))
                    // Ожидаем статус 201 (Created)
                    .andExpect(status().isCreated())
                    // Проверяем, что в ответе вернулось имя созданной конюшни
                    .andExpect(jsonPath("$.stableName", is("Test Stable")))
                    .andExpect(jsonPath("$.maxCapacity", is(10)));
        }

        @Test
        void testAddInvalidStable() throws Exception {
            // Пытаемся добавить конюшню с некорректными данными (емкость -5)
            String badStableJson = """
            {
                "name": "Bad Stable",
                "capacity": -5
            }
        """;

            mockMvc.perform(post("/api/stable")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(badStableJson))
                    // Ожидаем статус 400 (Bad Request), так как у нас есть валидация
                    .andExpect(status().isBadRequest());
        }
    }
}
