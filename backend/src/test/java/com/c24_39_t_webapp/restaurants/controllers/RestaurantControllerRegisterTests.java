package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.services.IRestaurantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = RestaurantController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
/**
 * Test para el endpoint de registro de restaurante
 * Verifica que tras registrar un restaurante con datos válidos,
 * se retorna el código 201 Created con los datos del restaurante creado
 * También verifica el caso de error:
 * - Excepción genérica lanzada por el servicio: retorna 500 Internal Server Error
 * Patrón AAA: Arrange, Act, Assert
 */
@DisplayName("RestaurantController - POST /api/restaurant (Register)")
class RestaurantControllerRegisterTests {

    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String RESTAURANT_ENDPOINT = "/api/restaurant";
    private static final String VALID_EMAIL = "test@example.com";
    private static final long RESTAURANT_ID = 1L;  // El ID a actualizar/borrar

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

    // Setup compartido para register
    private RestaurantRequestDto validRestaurantDto;

    private RestaurantResponseDto expectedRestaurantResponse;

    /**
     * Configuración común para los test de creación de restaurantes
     * Crea un RestaurantRequestDto válido y un UserDetailsImpl válido antes de cada test
     */
    @BeforeEach
    void setUp() {
        // Arrange común para todos los tests de registro
        validRestaurantDto = new RestaurantRequestDto(
                RESTAURANT_ID,
                "Atlántico",
                "Deliciosas recetas marineras",
                1L,
                "555 666 777",
                VALID_EMAIL,
                "Calle Arriba 11",
                "10-15 h y 20-24 h",
                "https://example.com/logo.png",
                "https://example.com/cover.jpg"
        );

        expectedRestaurantResponse = new RestaurantResponseDto(
                RESTAURANT_ID,
                1L,
                "Atlántico",
                "Deliciosas recetas marineras",
                "555 666 777",
                VALID_EMAIL,
                "Calle Arriba 11",
                "10-15 h y 20-24 h",
                "https://ejemplo.com/logo.jpg",
                "https://ejemplo.com/cover.jpg",
                1L,
                "Mediterránea"
        );
    }

    /**
     * Test que verifica que al intentar registrar un restaurante con datos válidos
     * se retorna el código 201 con los datos del restaurante creado.
     * Arrange: Configura el mock del servicio para que retorne el RestauranteResponseDto esperado
     * Act & Assert: Realiza la petición POST y verifica el status 201 y el contenido de la respuesta
     * Verify: Verifica que el servicio se llamó una vez con los parámetros correctos
     *
     * @throws Exception
     */
    @Test
    @DisplayName("POST /api/restaurant - Retorna 200 al crear un restaurante")
    void whenRegisterRestaurantWithValidData_thenReturnsCreatedWithRestaurantData() throws Exception {
        // Arrange
        when(restaurantService.registerRestaurant(eq(validRestaurantDto), eq(VALID_EMAIL)))
                .thenReturn(expectedRestaurantResponse);

        // Act & Assert
        mockMvc.perform(post(RESTAURANT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRestaurantDto))
                        .with(user(VALID_EMAIL).roles("RESTAURANTE"))
                        .with(csrf()))
//                .andDo(print())  // ← AQUÍ: IMPRIME TO-DO
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rst_id").value(RESTAURANT_ID))
                .andExpect(jsonPath("$.name").value("Atlántico"))
                .andExpect(jsonPath("$.cuisineName").value("Mediterránea"));

        // Verify
        verify(restaurantService, times(1)).registerRestaurant(eq(validRestaurantDto), eq(VALID_EMAIL));
    }

    /**
     * Test que verifica que al intentar registrar un restaurante, si el servicio lanza una excepción genérica,
     * se retorna el código 500 Internal Server Error.
     * Arrange: Configura el mock del servicio para que lance RuntimeException
     * Act & Assert: Realiza la petición POST y verifica el status 500
     * Verify: Verifica que el servicio se llamó una vez con los parámetros correctos
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Fail POST /api/restaurant - Retorna 500 si el servicio lanza excepción")
    void whenServiceThrowsException_thenReturnsInternalError() throws Exception {
        // Arrange
        when(restaurantService.registerRestaurant(eq(validRestaurantDto), eq(VALID_EMAIL)))
                .thenThrow(new RuntimeException("Error en BD"));

        // Act & Assert
        mockMvc.perform(post(RESTAURANT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRestaurantDto))
                        .with(user(VALID_EMAIL).roles("RESTAURANTE"))
                        .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(restaurantService, times(1)).registerRestaurant(eq(validRestaurantDto), eq(VALID_EMAIL));
    }
}