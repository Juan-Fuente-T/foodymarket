package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.CategoryFactory;
import com.c24_39_t_webapp.restaurants.services.ICategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para los endpoints GET de CategoryController
 * Verifica que al obtener todas las categorías, se retorna el código 200 OK con la lista
 * Verifica que al obtener una categoría por ID válido, se retorna 200 OK con los datos de la categoría
 * También verifica los casos de error:
 * - CategoryNotFoundException: cuando la categoría no existe. Retorna 404 Not Found
 * - ID inválido (≤ 0): retorna 404 Not Found
 * - Petición sin autenticación: retorna 401 Unauthorized
 *
 * Patrón AAA: Arrange, Act, Assert
 */
@WebMvcTest(
        controllers = CategoryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@Slf4j
@DisplayName("CategoryController - GET /api/category/*")
public class CategoryControllerGetTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String CATEGORY_ENDPOINT = "/api/category";
    private static final String VALID_EMAIL = "test@example.com";
    private static final long CATEGORY_ID = 1L;

    /**
     * MockMvc para simular peticiones HTTP al controlador
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mock del servicio de categorías
     */
    @MockitoBean
    private ICategoryService categoryService;

    /**
     * ObjectMapper para convertir objetos Java a JSON
     */
    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET ALL CATEGORIES ====================

    @Nested
    @DisplayName("GET /api/category (Get All Categories)")
    class GetAllCategoriesTests {

        private List<CategoryResponseDto> mockCategoriesList;

        @BeforeEach
        void setUp() {
            mockCategoriesList = CategoryFactory.responseListDefault();
        }

        /**
         * Test que verifica que al obtener todas las categorías, se retorna 200 OK con la lista de categorías
         * Arrange: Configura el mock del servicio para retornar la lista de categorías
         * Act & Assert: Realiza la petición GET CON autenticación y verifica el status 200 y la lista
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/category - Retorna 200 OK con lista de todas las categorías")
        void whenGetAllCategories_thenReturnsOkWithCategoriesList() throws Exception {
            log.info("Iniciando test de obtención de todas las categorías");

            // Arrange
            when(categoryService.findAllCategories())
                    .thenReturn(mockCategoriesList);

            // Act & Assert - CON autenticación (require RESTAURANTE)
            mockMvc.perform(get(CATEGORY_ENDPOINT)
                            .with(user(VALID_EMAIL)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(mockCategoriesList.size())));

            // Verify
            verify(categoryService, times(1)).findAllCategories();
        }

        /**
         * Test que verifica que al obtener todas las categorías sin autenticación,
         * se retorna 401 Unauthorized
         * Arrange: No se configura autenticación
         * Act & Assert: Realiza la petición sin auth y verifica el status 401
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/category - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(get(CATEGORY_ENDPOINT))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NO fue llamado
            verify(categoryService, never()).findAllCategories();
        }
    }

    // ==================== GET CATEGORY BY ID ====================

    @Nested
    @DisplayName("GET /api/category/{ctg_id} (Get Category By ID)")
    class GetCategoryByIdTests {

        private CategoryResponseDto expectedCategory;

        @BeforeEach
        void setUp() {
            expectedCategory = CategoryFactory.defaultResponse(CATEGORY_ID);
        }

        /**
         * Test que verifica que al obtener una categoría por ID válido,
         * se retorna 200 OK con los datos de la categoría
         * Arrange: Configura el mock del servicio para retornar la categoría
         * Act & Assert: Realiza la petición GET CON autenticación y verifica el status 200 y los datos
         * Verify: Verifica que el servicio se llamó una sola vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/category/{ctg_id} - Retorna 200 OK con datos de la categoría")
        void whenGetCategoryById_thenReturnsOkWithCategoryData() throws Exception {
            log.info("Iniciando test de obtención de categoría por ID");

            // Arrange
            when(categoryService.findCategoryById(eq(CATEGORY_ID)))
                    .thenReturn(expectedCategory);

            // Act & Assert - CON autenticación
            mockMvc.perform(get(CATEGORY_ENDPOINT + "/" + CATEGORY_ID)
                            .with(user(VALID_EMAIL)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ctg_id").value(CATEGORY_ID))
                    .andExpect(jsonPath("$.name").value(expectedCategory.name()));

            // Verify
            verify(categoryService, times(1)).findCategoryById(eq(CATEGORY_ID));
        }

        /**
         * Test que verifica que al obtener una categoría que no existe,
         * se retorna 404 Not Found
         * Arrange: Configura el mock del servicio para lanzar CategoryNotFoundException
         * Act & Assert: Realiza la petición GET y verifica el status 404
         * Verify: Verifica que el servicio se llamó una sola vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/category/{ctg_id} - Retorna 404 si la categoría no existe")
        void whenCategoryNotFound_thenReturns404() throws Exception {
            // Arrange
            when(categoryService.findCategoryById(eq(999L)))
                    .thenThrow(new CategoryNotFoundException("No se encontro una categoria con ese ID: 999"));

            // Act & Assert - CON autenticación
            mockMvc.perform(get(CATEGORY_ENDPOINT + "/999")
                            .with(user(VALID_EMAIL)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("CategoryNotFoundException"));

            // Verify
            verify(categoryService, times(1)).findCategoryById(eq(999L));
        }

        /**
         * Test que verifica que al obtener una categoría con ID inválido (≤ 0),
         * se retorna 404 Not Found
         * Arrange: El servicio lanza CategoryNotFoundException para ID inválido
         * Act & Assert: Realiza la petición GET con ID inválido y verifica el status 404
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/category/{ctg_id} - Retorna 404 si el ID es inválido (≤ 0)")
        void whenCategoryIdInvalid_thenReturns404() throws Exception {
            // Arrange
            when(categoryService.findCategoryById(eq(0L)))
                    .thenThrow(new CategoryNotFoundException("El ID de la categoria no es válido 0"));

            // Act & Assert - CON autenticación
            mockMvc.perform(get(CATEGORY_ENDPOINT + "/0")
                            .with(user(VALID_EMAIL)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("CategoryNotFoundException"));

            // Verify
            verify(categoryService, times(1)).findCategoryById(eq(0L));
        }

        /**
         * Test que verifica que al obtener una categoría sin autenticación,
         * se retorna 401 Unauthorized
         * Arrange: No se configura autenticación
         * Act & Assert: Realiza la petición GET sin auth y verifica el status 401
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/category/{ctg_id} - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(get(CATEGORY_ENDPOINT + "/" + CATEGORY_ID)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NO fue llamado
            verify(categoryService, never()).findCategoryById(any());
        }
    }
}