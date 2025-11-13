package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.CategoryRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.CategoryFactory;
import com.c24_39_t_webapp.restaurants.services.IRestaurantService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para los endpoints de categorías en RestaurantController
 * Verifica que al obtener las categorías de un restaurante, se retorna el código 200 OK con el conjunto de categorías
 * Verifica que al añadir una categoría a un restaurante, se retorna el código 201 Created con la categoría añadida
 * También verifica los casos de error:
 * - RestaurantNotFoundException: cuando el restaurante no existe. Retorna 404 Not Found
 * - Petición sin autenticación: retorna 401 Unauthorized
 *
 * NOTA: Ambos endpoints requieren @PreAuthorize("hasRole('RESTAURANTE')")
 *
 * Patrón AAA: Arrange, Act, Assert
 */
@WebMvcTest(
        controllers = RestaurantController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@Slf4j
@DisplayName("RestaurantController - Category Endpoints (GET/POST /api/restaurant/{id}/categories)")
public class RestaurantControllerCategoryTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String RESTAURANT_ENDPOINT = "/api/restaurant";
    private static final String VALID_EMAIL = "restaurante@example.com";
    private static final long RESTAURANT_ID = 1L;
    private static final long CATEGORY_ID = 1L;

    /**
     * MockMvc para simular peticiones HTTP al controlador
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mock del servicio de restaurantes
     */
    @MockitoBean
    private IRestaurantService restaurantService;

    /**
     * ObjectMapper para convertir objetos Java a JSON
     */
    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET OFFERED CATEGORIES ====================

    @Nested
    @DisplayName("GET /api/restaurant/{restaurantId}/categories")
    class GetOfferedCategoriesTests {

        private Set<CategoryResponseDto> mockCategoriesSet;
        private CategoryResponseDto expectedCategory;

        @BeforeEach
        void setUp() {
            // Arrange común para GET categories
            mockCategoriesSet = CategoryFactory.responseSetDefault();
            expectedCategory = CategoryFactory.defaultResponse(CATEGORY_ID);
        }

        /**
         * Test que verifica que al obtener las categorías de un restaurante válido,
         * se retorna el código 200 OK con el conjunto de categorías ofrecidas
         * Arrange: Configura el mock del servicio para retornar un conjunto de categorías
         * Act & Assert: Realiza la petición GET CON autenticación y rol RESTAURANTE, verifica el status 200 y el contenido
         * Verify: Verifica que el servicio se llamó una sola vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/restaurant/{id}/categories - Retorna 200 OK con categorías del restaurante")
        void whenGetOfferedCategories_thenReturnsOkWithCategories() throws Exception {
            log.info("Iniciando test de obtención de categorías");

            // Arrange
            when(restaurantService.findByIdFetchingCategories(eq(RESTAURANT_ID)))
                    .thenReturn(mockCategoriesSet);

            // Act & Assert - CON autenticación y rol RESTAURANTE
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/" + RESTAURANT_ID + "/categories")
                            .with(user(VALID_EMAIL).roles("RESTAURANTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(mockCategoriesSet.size())));

            // Verify
            verify(restaurantService, times(1)).findByIdFetchingCategories(eq(RESTAURANT_ID));
        }

        /**
         * Test que verifica que al obtener categorías de un restaurante que no existe,
         * se retorna el código 404 Not Found
         * Arrange: Configura el mock del servicio para lanzar RestaurantNotFoundException
         * Act & Assert: Realiza la petición GET CON autenticación y verifica el status 404
         * Verify: Verifica que el servicio se llamó una sola vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/restaurant/{id}/categories - Retorna 404 si el restaurante no existe")
        void whenRestaurantNotFound_thenReturns404() throws Exception {
            // Arrange
            when(restaurantService.findByIdFetchingCategories(eq(999L)))
                    .thenThrow(new RestaurantNotFoundException("Restaurante no encontrado con ID: 999"));

            // Act & Assert - CON autenticación y rol RESTAURANTE
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/999/categories")
                            .with(user(VALID_EMAIL).roles("RESTAURANTE")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("RestaurantNotFoundException"));

            // Verify
            verify(restaurantService, times(1)).findByIdFetchingCategories(eq(999L));
        }

        /**
         * Test que verifica que al intentar obtener categorías sin autenticación,
         * se retorna el código 401 Unauthorized
         * Arrange: No se configura autenticación
         * Act & Assert: Realiza la petición GET sin auth y verifica el status 401
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/restaurant/{id}/categories - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/" + RESTAURANT_ID + "/categories"))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NO fue llamado
            verify(restaurantService, never()).findByIdFetchingCategories(any());
        }
    }

    // ==================== ADD CATEGORY TO RESTAURANT ====================

    @Nested
    @DisplayName("POST /api/restaurant/{restaurantId}/categories")
    class AddCategoryToRestaurantTests {

        private CategoryRequestDto validCategoryDto;
        private CategoryResponseDto expectedCategoryResponse;

        @BeforeEach
        void setUp() {
            // Arrange común para POST add category
            validCategoryDto = CategoryFactory.defaultRequest();
            expectedCategoryResponse = CategoryFactory.responseFromRequest(validCategoryDto, CATEGORY_ID);
        }

        /**
         * Test que verifica que al añadir una categoría a un restaurante válido,
         * se retorna el código 201 Created con los datos de la categoría añadida
         * Arrange: Configura el mock del servicio para retornar la categoría añadida
         * Act & Assert: Realiza la petición POST CON autenticación y rol RESTAURANTE, verifica el status 201 y los datos retornados
         * Verify: Verifica que el servicio se llamó una sola vez con los parámetros correctos
         *
         * @throws Exception
         */
        @Test
        @DisplayName("POST /api/restaurant/{id}/categories - Retorna 201 Created al añadir una categoría")
        void whenAddCategoryWithValidData_thenReturnsCreatedWithCategoryData() throws Exception {
            log.info("Iniciando test de adición de categoría");

            // Arrange
            when(restaurantService.addCategoryToRestaurant(eq(RESTAURANT_ID), any(CategoryRequestDto.class)))
                    .thenReturn(expectedCategoryResponse);

            // Act & Assert - CON autenticación, rol RESTAURANTE y CSRF
            mockMvc.perform(post(RESTAURANT_ENDPOINT + "/" + RESTAURANT_ID + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCategoryDto))
                            .with(user(VALID_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.ctg_id").value(CATEGORY_ID))
                    .andExpect(jsonPath("$.name").value(expectedCategoryResponse.name()));

            // Verify
            verify(restaurantService, times(1)).addCategoryToRestaurant(eq(RESTAURANT_ID), any(CategoryRequestDto.class));
        }

        /**
         * Test que verifica que al intentar añadir una categoría a un restaurante inexistente,
         * se retorna el código 404 Not Found
         * Arrange: Configura el mock del servicio para lanzar RestaurantNotFoundException
         * Act & Assert: Realiza la petición POST CON autenticación y verifica el status 404
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail POST /api/restaurant/{id}/categories - Retorna 404 si el restaurante no existe")
        void whenRestaurantNotFound_thenReturns404() throws Exception {
            // Arrange
            when(restaurantService.addCategoryToRestaurant(eq(999L), any(CategoryRequestDto.class)))
                    .thenThrow(new RestaurantNotFoundException("Restaurante no encontrado con ID: 999"));

            // Act & Assert - CON autenticación, rol RESTAURANTE y CSRF
            mockMvc.perform(post(RESTAURANT_ENDPOINT + "/999/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCategoryDto))
                            .with(user(VALID_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("RestaurantNotFoundException"));

            // Verify
            verify(restaurantService, times(1)).addCategoryToRestaurant(eq(999L), any(CategoryRequestDto.class));
        }

        /**
         * Test que verifica que al intentar añadir una categoría sin autenticación,
         * se retorna el código 401 Unauthorized
         * Arrange: No se configura autenticación
         * Act & Assert: Realiza la petición POST sin auth y verifica el status 401
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail POST /api/restaurant/{id}/categories - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(post(RESTAURANT_ENDPOINT + "/" + RESTAURANT_ID + "/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCategoryDto))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NO fue llamado
            verify(restaurantService, never()).addCategoryToRestaurant(any(), any());
        }
    }
}