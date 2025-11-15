package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.services.IUserService;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test para el endpoint DELETE de UserController
 * Verifica que al borrar el perfil de un usuario, se retorna 204 No Content
 * También verifica los casos de error:
 * - UserNotFoundException: cuando el usuario no existe. Retorna 404 Not Found
 * - UnauthorizedAccessException: cuando el usuario no tiene permiso para borrar su perfil. Retorna 403 Forbidden
 * - Petición sin autenticación: retorna 401 Unauthorized
 * - Sin rol CLIENTE: retorna 403 Forbidden
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
@DisplayName("UserController - DELETE /api/user (Delete User)")
public class UserControllerDeleteTests {
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

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - DELETE /api/user")
    class SuccessCases {

        /**
         * Test que verifica que al borrar el perfil de un usuario válido,
         * se retorna el código 204 No Content
         * Arrange: Prepara el UserDetailsImpl con rol CLIENTE
         * Act & Assert: Realiza la petición DELETE CON autenticación y verifica el status 204
         * Verify: Verifica que el servicio se llamó una sola vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("DELETE /api/user - Retorna 204 No Content al borrar la cuenta")
        void whenDeleteUserWithValidId_thenReturnsNoContent() throws Exception {
            log.info("Test: Borrar cuenta de usuario");

            // Arrange
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            doNothing().when(userService).deleteUser(eq(USER_ID));

            // Act & Assert - Mock de autenticación con UserDetailsImpl
            mockMvc.perform(delete(USER_ENDPOINT)
                            .with(authentication(new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities())))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // Verify
            verify(userService, times(1)).deleteUser(eq(USER_ID));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - DELETE /api/user")
    class ErrorCases {

        /**
         * Test que verifica que al intentar borrar un usuario que no existe,
         * se retorna 404 Not Found
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/user - Retorna 404 si el usuario no existe")
        void whenUserNotFound_thenReturns404() throws Exception {
            // Arrange
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            doThrow(new UserNotFoundException("No se ha encontrado el usuario"))
                    .when(userService).deleteUser(eq(USER_ID));

            // Act & Assert
            mockMvc.perform(delete(USER_ENDPOINT)
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("UserNotFoundException"));

            // Verify
            verify(userService, times(1)).deleteUser(eq(USER_ID));
        }

        /**
         * Test que verifica que sin autenticación, se retorna 401 Unauthorized
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/user - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación y CSRF
            mockMvc.perform(delete(USER_ENDPOINT)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NO fue llamado
            verify(userService, never()).deleteUser(any());
        }

        /**
         * Test que verifica que sin rol CLIENTE, se retorna 403 Forbidden
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/user - Retorna 403 si intenta borrar otra cuenta")
        void whenTryingToDeleteOtherUserAccount_thenReturnsForbidden() throws Exception {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            // Mock lanza UnauthorizedAccessException
            doThrow(new UnauthorizedAccessException("No puedes borrar la cuenta de otro usuario"))
                    .when(userService).deleteUser(eq(USER_ID));

            mockMvc.perform(delete(USER_ENDPOINT)
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));

            verify(userService, times(1)).deleteUser(eq(USER_ID));
        }
    }
}