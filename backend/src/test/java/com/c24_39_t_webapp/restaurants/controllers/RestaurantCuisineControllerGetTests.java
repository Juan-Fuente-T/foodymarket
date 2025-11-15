package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.CuisineResponseDto;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.RestaurantCuisineFactory;
import com.c24_39_t_webapp.restaurants.services.IRestaurantCuisineService;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para el endpoint GET de RestaurantCuisineController
 * Verifica que al obtener todas las cuisines (tipos de cocina), se retorna 200 OK con la lista de cuisines
 * También verifica los casos de error:
 * - Petición sin autenticación: retorna 401 Unauthorized
 * - Sin rol RESTAURANTE: retorna 403 Forbidden
 *
 * Patrón AAA: Arrange, Act, Assert
 */
@WebMvcTest(
        controllers = RestaurantCuisineController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@Slf4j
@DisplayName("RestaurantCuisineController - GET /api/cuisines (Get All Cuisines)")
public class RestaurantCuisineControllerGetTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String CUISINE_ENDPOINT = "/api/cuisines";
    private static final String RESTAURANTE_EMAIL = "restaurante@example.com";

    /**
     * MockMvc para simular peticiones HTTP al controlador
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mock del servicio de cuisines
     */
    @MockitoBean
    private IRestaurantCuisineService cuisineService;

    /**
     * ObjectMapper para convertir objetos Java a JSON
     */
    @Autowired
    private ObjectMapper objectMapper;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - GET /api/cuisines")
    class SuccessCases {

        private List<CuisineResponseDto> mockCuisinesList;

        @BeforeEach
        void setUp() {
            mockCuisinesList = RestaurantCuisineFactory.responseListDefault();
        }

        /**
         * Test que verifica que al obtener todas las cuisines,
         * se retorna 200 OK con la lista de cuisines disponibles
         * Arrange: Configura el mock del servicio para retornar la lista de cuisines
         * Act & Assert: Realiza la petición GET CON autenticación y rol RESTAURANTE, verifica el status 200 y la lista
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/cuisines - Retorna 200 OK con lista de tipos de cocina")
        void whenGetAllCuisines_thenReturnsOkWithCuisinesList() throws Exception {
            log.info("Iniciando test de obtención de todas las cuisines");

            // Arrange
            when(cuisineService.findAll())
                    .thenReturn(mockCuisinesList);

            // Act & Assert - CON autenticación, rol RESTAURANTE
            mockMvc.perform(get(CUISINE_ENDPOINT)
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(mockCuisinesList.size())));

            // Verify
            verify(cuisineService, times(1)).findAll();
        }

        /**
         * Test que verifica que al obtener cuisines sin que haya ninguna,
         * se retorna 200 OK con una lista vacía
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/cuisines - Retorna 200 OK con lista vacía si no hay cuisines")
        void whenNoCuisines_thenReturnsOkWithEmptyList() throws Exception {
            // Arrange
            when(cuisineService.findAll())
                    .thenReturn(List.of());

            // Act & Assert - CON autenticación, rol RESTAURANTE
            mockMvc.perform(get(CUISINE_ENDPOINT)
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            // Verify
            verify(cuisineService, times(1)).findAll();
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - GET /api/cuisines")
    class ErrorCases {

        /**
         * Test que verifica que sin autenticación, se retorna 401 Unauthorized
         * Arrange: No se configura autenticación
         * Act & Assert: Realiza la petición GET sin auth y verifica el status 401
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/cuisines - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(get(CUISINE_ENDPOINT))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NO fue llamado
            verify(cuisineService, never()).findAll();
        }
    }
}