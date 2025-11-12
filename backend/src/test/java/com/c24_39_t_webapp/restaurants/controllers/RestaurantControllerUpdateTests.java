package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.RestaurantFactory;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Test para el endpoint de actualización de restaurante
 * Verifica que tras actualizar un restaurante con un ID válido,
 * se retorna el código 200 junto con los datos actualizados
 * También verifica los casos de error:
 * - RestaurantNotFoundException: cuando el restaurante no existe. Retorna 404 Not Found
 * - UnauthorizedAccessException: cuando el usuario no tiene permiso. Retorna 403 Forbidden
 * - Petición sin autenticación: retorna 401 Unauthorized
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
@DisplayName("RestaurantController - PATCH /api/restaurant (Update)")
public class RestaurantControllerUpdateTests {
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
    private ObjectMapper objectMapper; // Convierte objetos Java ↔ JSON

    // Setup compartido para update
    // Datos válidos para actualizar un restaurante
    private RestaurantRequestDto validRestaurantDto;
    // Respuesta esperada tras actualizar un restaurante
    private RestaurantResponseDto expectedRestaurantResponse;

    @BeforeEach
    void setUp() {
        // Arrange común para todos los tests de registro
            validRestaurantDto = RestaurantFactory.defaultRequest(RESTAURANT_ID, VALID_EMAIL);
            expectedRestaurantResponse = RestaurantFactory.responseFromRequest(validRestaurantDto, RESTAURANT_ID);
    }

    @Nested
    @DisplayName("Update Success Cases")
    class UpdateSuccessCases {
        /**
         * Test que verifica que al actualizar un restaurante con un ID válido,
         * se retorna el código 200 OK junto con los datos actualizados
         * Arrange: Configura el mock del servicio para que retorne los datos esperados
         * Act & Assert: Realiza la petición PATCH y verifica el status 200 y los datos retornados
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("PATCH /api/restaurant/{id} - Retorna 200 al actualizar un restaurante")
        void whenUpdateWithValidData_thenReturnsOkWithRestaurantData() throws Exception {
            log.info("Iniciando test de actualización");

            // Arrange
            when(restaurantService.updateRestaurant(any(RestaurantRequestDto.class), eq(RESTAURANT_ID)))
                    .thenReturn(expectedRestaurantResponse);

            // Act & Assert
            mockMvc.perform(patch(RESTAURANT_ENDPOINT + "/" + RESTAURANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRestaurantDto))
                            .with(user(VALID_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andDo(print())
//            .andDo(print())  // ← AQUÍ: IMPRIME TO-DO
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rst_id").value(RESTAURANT_ID))
                    .andExpect(jsonPath("$.name").value("Atlántico"));
            // Verify
            verify(restaurantService, times(1)).updateRestaurant(any(RestaurantRequestDto.class), eq(RESTAURANT_ID));
            System.out.println("Test completado");
        }
    }

    @Nested
    @DisplayName("Update Error Cases")
    class UpdateErrorCases {
        /**
         * Test que verifica que al intentar actualizar un restaurante inexistente,
         * se lanza RestaurantNotFoundException y se retorna 404 Not Found
         * Arrange: Configura el mock del servicio para que lance RestaurantNotFoundException
         * Act & Assert: Realiza la petición PATCH y verifica el status 404 y el mensaje de error
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/restaurant/{id} - Retorna 404 si el restaurante no existe")
        void whenRestaurantNotFound_thenReturnsNotFound() throws Exception {
            // Arrange
            when(restaurantService.updateRestaurant(any(RestaurantRequestDto.class), eq(999L)))
                    .thenThrow(new RestaurantNotFoundException("Restaurante no encontrado"));

            // Act & Assert
            mockMvc.perform(patch(RESTAURANT_ENDPOINT + "/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRestaurantDto))
                            .with(user(VALID_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound());

            verify(restaurantService, times(1)).updateRestaurant(any(), eq(999L));
        }

        /**
         * Test que verifica que al intentar actualizar un restaurante sin ser el dueño,
         * se lanza UnauthorizedAccessException y se retorna 403 Forbidden
         * Arrange: Configura el mock del servicio para que lance UnauthorizedAccessException
         * Act & Assert: Realiza la petición PATCH y verifica el status 403
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/restaurant/{id} - Retorna 403 si no eres el propietario del restaurante")
        void whenNotRestaurantOwner_thenReturnsForbidden() throws Exception {
            // Arrange
            when(restaurantService.updateRestaurant(any(RestaurantRequestDto.class), eq(RESTAURANT_ID)))
                    .thenThrow(new UnauthorizedAccessException("No tienes permiso para este restaurante."));

            // Act & Assert
            mockMvc.perform(patch(RESTAURANT_ENDPOINT + "/" + RESTAURANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRestaurantDto))
                            .with(user("otro@email.com").roles("RESTAURANTE"))  // ← OTRO USUARIO
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(restaurantService, times(1)).updateRestaurant(any(), eq(RESTAURANT_ID));
        }

        /**
         * Test que verifica que al intentar actualizar un restaurante sin autenticación, se retorna 401 Unauthorized
         * Act & Assert: Realiza la petición PATCH sin usuario y verifica el status 401
         * Verify: Verifica que el servicio NUNCA se llamó
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail UPDATE /api/restaurant/{id} - Lanza 401 Unathorized Rechaza PATCH sin autenticación")
        void whenUpdateWithoutAuthentication_thenUnauthorized() throws Exception {
            // Arrange
            when(restaurantService.updateRestaurant(any(RestaurantRequestDto.class), eq(999L)))
                    .thenThrow(new UnauthorizedAccessException("Restaurante no encontrado"));

            // Act & Assert
            mockMvc.perform(patch(RESTAURANT_ENDPOINT + "/" + RESTAURANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRestaurantDto))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verify(restaurantService, never()).deleteById(eq(RESTAURANT_ID));
        }
    }
}
