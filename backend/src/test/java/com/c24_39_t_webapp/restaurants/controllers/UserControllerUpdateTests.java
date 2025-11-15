package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.UserFactory;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.services.IUserService;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para el endpoint PUT de UserController
 * Verifica que al actualizar un usuario con datos válidos, se retorna 200 OK con los datos actualizados
 * También verifica los casos de error:
 * - UserNotFoundException: cuando el usuario no existe. Retorna 404 Not Found
 * - UnauthorizedAccessException: cuando el usuario no tiene permiso para actualizar su perfil. Retorna 403 Forbidden
 * - Petición sin autenticación: retorna 401 Unauthorized
 * - Sin rol CLIENTE: retorna 403 Forbidden
 * <p>
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
@DisplayName("UserController - PUT /api/user (Update User)")
public class UserControllerUpdateTests {
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
    @DisplayName("Success Cases - PUT /api/user")
    class SuccessCases {

        private UserRequestDto validUpdateDto;
        private UserResponseDto expectedUserResponse;

        @BeforeEach
        void setUp() {
            validUpdateDto = UserFactory.requestWith("Juan García", CLIENT_EMAIL, "CLIENTE", "9876543210", "Calle de Arriba 11", "NewPassword987");
            expectedUserResponse = UserFactory.responseFromRequest(validUpdateDto, USER_ID);
        }

        /**
         * Test que verifica que al actualizar un usuario con datos válidos,
         * se retorna 200 OK con los datos actualizados
         * Arrange: Configura el mock del servicio para retornar el usuario actualizado
         * Act & Assert: Realiza la petición PUT CON autenticación y verifica el status 200
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("PUT /api/user - Retorna 200 OK al actualizar perfil válido")
        void whenUpdateUserWithValidData_thenReturnsOkWithUpdatedData() throws Exception {
            log.info("Test: Actualizar perfil de usuario");

            // Arrange
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            when(userService.updateUser(eq(USER_ID), any(UserRequestDto.class)))
                    .thenReturn(expectedUserResponse);

            // Act & Assert
            mockMvc.perform(put(USER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.email").value(CLIENT_EMAIL));

            // Verify
            verify(userService, times(1)).updateUser(eq(USER_ID), any(UserRequestDto.class));
        }

    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - PUT /api/user")
    class ErrorCases {

        private UserRequestDto validUpdateDto;

        @BeforeEach
        void setUp() {
            validUpdateDto = UserFactory.defaultRequest();
        }

        /**
         * Test que verifica que al intentar actualizar un usuario que no existe,
         * se retorna 404 Not Found
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PUT /api/user - Retorna 404 si el usuario no existe")
        void whenUserNotFound_thenReturns404() throws Exception {
            // Arrange
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            when(userService.updateUser(eq(USER_ID), any(UserRequestDto.class)))
                    .thenThrow(new UserNotFoundException("No se ha encontrado el usuario"));

            // Act & Assert
            mockMvc.perform(put(USER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(authentication(new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities())))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("UserNotFoundException"));

            // Verify
            verify(userService, times(1)).updateUser(eq(USER_ID), any(UserRequestDto.class));
        }

        /**
         * Test que verifica que sin autenticación, se retorna 401 Unauthorized
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PUT /api/user - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(put(USER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NO fue llamado
            verify(userService, never()).updateUser(any(), any());
        }

        /**
         * Test que verifica que al intentar actualizar otra cuenta de usuario que no es la propia,
         * se retorna 403 Forbidden
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PUT /api/user - Retorna 403 si intenta actualizar otra cuenta")
        void whenTryingToUpdateOtherUserAccount_thenReturnsForbidden() throws Exception {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("RESTAURANTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            doThrow(new UnauthorizedAccessException("No puedes actualizar la cuenta de otro usuario"))
                    .when(userService).updateUser(eq(USER_ID), any(UserRequestDto.class));

            mockMvc.perform(put(USER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));

            verify(userService, times(1)).updateUser(eq(USER_ID), any(UserRequestDto.class));
        }
    }
}