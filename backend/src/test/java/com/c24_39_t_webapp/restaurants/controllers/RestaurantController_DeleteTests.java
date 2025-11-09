package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.services.IRestaurantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RestaurantController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
/**
 * Test para el endpoint de borrado de restaurante
 * Verfica que tras borrar un restaurante con un ID válido,
 * se retorna el código 204 No Content
 * También verifica los casos de error:
 * - RestaurantNotFoundException: cuando el restaurante no existe. Retorna 404 Not Found
 * - UnauthorizedAccessException: cuando el usuario no tiene permiso. Retorna 403 Forbidden
 * - Petición sin autenticación: retorna 401 Unauthorized
 * Patrón AAA: Arrange, Act, Assert
 */
@Slf4j
@DisplayName("RestaurantController - DELETE /api/restaurant{id} (Delete)")
public class RestaurantController_DeleteTests {
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


    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String RESTAURANT_ENDPOINT = "/api/restaurant";
    private static final String VALID_EMAIL = "test@example.com";
    private static final long RESTAURANT_ID = 1L;  // El ID a actualizar/borrar

    /**
     * Test que verifica que al borrar un restaurante con un ID válido, se retorna el código 204 No Content
     * Arrange: Configura el mock del servicio para que no haga nada al borrar
     * Act & Assert: Realiza la petición DELETE y verifica el status 204
     * Verify: Verifica que el servicio se llamó una vez con el ID correcto
     *
     * @throws Exception
     */
    @Test
    @DisplayName("DELETE /api/restaurant/{id} - Retorna 204 al borrar un restaurante")
    void whenDeleteRestaurantWithValidId_thenReturnsNoContent() throws Exception {
        // Arrange: No hace nada, solo configura que no lance excepción
        doNothing().when(restaurantService).deleteById(RESTAURANT_ID);

        // Act & Assert
        mockMvc.perform(delete(RESTAURANT_ENDPOINT + "/" + RESTAURANT_ID)
                        .with(user(VALID_EMAIL).roles("RESTAURANTE"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
        // Verify
        verify(restaurantService, times(1)).deleteById(RESTAURANT_ID);
    }

    /**
     * Test que verifica que al intentar borrar un restaurante inexistente, se lanza RestaurantNotFoundException y se retorna 404 Not Found
     * Arrange: Configura el mock del servicio para que lance RestaurantNotFoundException
     * Act & Assert: Realiza la petición DELETE y verifica el status 404 y el mensaje de error
     * Verify: Verifica que el servicio se llamó una vez con el ID correcto
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Fail DELETE /api/restaurant/{id} - Lanza 404 RestaurantNotFoundException si el restaurante no existe")
    void whenDeleteNonexistentRestaurant_thenThrowsNotFoundException() throws Exception {
        // Arrange
        doThrow(new RestaurantNotFoundException("Restaurante no encontrado con id: " + RESTAURANT_ID))
                .when(restaurantService).deleteById(eq(RESTAURANT_ID));

        // Act & Assert
        mockMvc.perform(delete("/api/restaurant/{rst_id}", RESTAURANT_ID)
                        .with(user(VALID_EMAIL).roles("RESTAURANTE"))
                        .with(csrf()))
//                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());

        // Verify
        verify(restaurantService, times(1)).deleteById(RESTAURANT_ID);
    }

    /**
     * Test que verifica que al intentar borrar un restaurante sin ser el dueño, se lanza UnauthorizedAccessException y se retorna 403 Forbidden
     * Arrange: Configura el mock del servicio para que lance UnauthorizedAccessException
     * Act & Assert: Realiza la petición DELETE y verifica el status 403
     * Verify: Verifica que el servicio se llamó una vez con el ID correcto
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Fail DELETE /api/restaurant/{id} - Retorna 403 Forbidden si el usuario no es el dueño")
    void whenDeleteRestaurantAsNonOwner_thenReturnsForbidden() throws Exception {
        // Arrange
        doThrow(new UnauthorizedAccessException("No tienes permiso para ver los restaurantes de este dueño."))
                .when(restaurantService).deleteById(eq(RESTAURANT_ID));

        // Act & Assert
        mockMvc.perform(delete("/api/restaurant/{rst_id}", RESTAURANT_ID)
                        .with(user("otro@inexistente.com").roles("RESTAURANTE"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        // Verify
        verify(restaurantService, times(1)).deleteById(eq(RESTAURANT_ID));
    }

    /**
     * Test que verifica que al intentar borrar un restaurante sin autenticación, se retorna 401 Unauthorized
     * Act & Assert: Realiza la petición DELETE sin usuario y verifica el status 401
     * Verify: Verifica que el servicio NUNCA se llamó
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Fail DELETE /api/restaurant/{id} - Lanza 401 Unathorized Rechaza DELETE sin autenticación")
    void whenDeleteWithoutAuthentication_thenUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/restaurant/{rst_id}", RESTAURANT_ID)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        // Verify que el servicio NUNCA se llamó
        verify(restaurantService, never()).deleteById(eq(RESTAURANT_ID));
    }
}
