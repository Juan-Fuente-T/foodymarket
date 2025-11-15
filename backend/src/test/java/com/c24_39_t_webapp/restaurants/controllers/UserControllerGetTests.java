package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.UserFactory;
import com.c24_39_t_webapp.restaurants.services.IUserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para el endpoint GET de UserController
 * Verifica que al obtener el perfil de un usuario autenticado, se retorna 200 OK con los datos del usuario
 * También verifica los casos de error:
 * - UserNotFoundException: cuando el usuario no existe. Retorna 404 Not Found
 * - Petición sin autenticación: retorna 401 Unauthorized
 *
 * Patrón AAA: Arrange, Act, Assert
 */
@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@Slf4j
@DisplayName("UserController - GET /api/user (Get User Profile)")
public class UserControllerGetTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String USER_ENDPOINT = "/api/user";
    private static final String CLIENT_EMAIL = "cliente@example.com";
    private static final long USER_ID = 1L;

    /**
     * MockMvc para simular peticiones HTTP al controlador
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mock del servicio de usuarios
     */
    @MockitoBean
    private IUserService userService;

    /**
     * ObjectMapper para convertir objetos Java a JSON
     */
    @Autowired
    private ObjectMapper objectMapper;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - GET /api/user")
    class SuccessCases {

        private UserResponseDto expectedUserResponse;

        @BeforeEach
        void setUp() {
            expectedUserResponse = UserFactory.defaultResponse(USER_ID);
        }

        /**
         * Test que verifica que al obtener el perfil de un usuario autenticado,
         * se retorna 200 OK con los datos del usuario
         * Arrange: Configura el mock del servicio para retornar el perfil del usuario
         * Act & Assert: Realiza la petición GET CON autenticación, verifica el status 200 y los datos
         * Verify: Verifica que el servicio se llamó una sola vez con el email correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/user - Retorna 200 OK con perfil del usuario")
        void whenGetUserProfile_thenReturnsOkWithUserData() throws Exception {
            log.info("Iniciando test de obtención de perfil de usuario");

            // Arrange
            when(userService.getUserProfile(eq(CLIENT_EMAIL)))
                    .thenReturn(expectedUserResponse);

            // Act & Assert - CON autenticación
            mockMvc.perform(get(USER_ENDPOINT)
                            .with(user(CLIENT_EMAIL).roles("CLIENTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.email").value(CLIENT_EMAIL));

            // Verify
            verify(userService, times(1)).getUserProfile(eq(CLIENT_EMAIL));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - GET /api/user")
    class ErrorCases {

        /**
         * Test que verifica que al obtener el perfil de un usuario que no existe,
         * se retorna 404 Not Found
         * Arrange: Configura el mock del servicio para lanzar UserNotFoundException
         * Act & Assert: Realiza la petición GET y verifica el status 404
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/user - Retorna 404 si el usuario no existe")
        void whenUserNotFound_thenReturns404() throws Exception {
            // Arrange
            when(userService.getUserProfile(eq(CLIENT_EMAIL)))
                    .thenThrow(new UserNotFoundException("No se ha encontrado el usuario"));

            // Act & Assert - CON autenticación
            mockMvc.perform(get(USER_ENDPOINT)
                            .with(user(CLIENT_EMAIL).roles("CLIENTE")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("UserNotFoundException"));

            // Verify
            verify(userService, times(1)).getUserProfile(eq(CLIENT_EMAIL));
        }

        /**
         * Test que verifica que sin autenticación, se retorna 401 Unauthorized
         * Arrange: No se configura autenticación
         * Act & Assert: Realiza la petición GET sin auth y verifica el status 401
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/user - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(get(USER_ENDPOINT))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NO fue llamado
            verify(userService, never()).getUserProfile(any());
        }
    }
}