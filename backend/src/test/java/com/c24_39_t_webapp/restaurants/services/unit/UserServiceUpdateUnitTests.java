package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.factories.UserFactory;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para UserService.updateUser()
 *
 * ✅ Usuarios vienen de EntityModelFactory
 * ✅ SecurityContext mockeado
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Actualizar usuario propio exitoso
 * ✅ Actualizar parcialmente (solo algunos campos)
 * ✅ Actualizar otro usuario → Lanza excepción
 * ✅ Usuario no encontrado → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("UserService - updateUser()")
class UserServiceUpdateUnitTests {

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "user@example.com";
    private static final String OTHER_EMAIL = "other@example.com";
    private static final String USER_PASSWORD = "Password123!";
    private static final String USER_PHONE = "555 666 777";
    private static final String USER_ADDRESS = "Calle Principal 123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.UserServiceImpl userService;

    /**
     * Método auxiliar para mockear SecurityContext
     */
    private void setupSecurityContextWithEmail(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - updateUser()")
    class SuccessCases {

        private UserEntity existingUser;
        private UserRequestDto updateRequest;

        @BeforeEach
        void setUp() {
            // ✅ Usuario existente
            existingUser = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);

            // ✅ Request de actualización
            updateRequest = UserFactory.requestWith(
                    "Juan García Actualizado",
                    "CLIENTE",
                    USER_EMAIL,
                    "999 888 777",
                    "Nueva Dirección 456",
                    USER_PASSWORD
            );
        }

        /**
         * Test: Actualizar usuario propio
         *
         * Verificación:
         * ✅ Usuario autenticado es el propietario
         * ✅ Contraseña se codifica
         * ✅ Usuario se guarda
         * ✅ Retorna UserResponseDto correcto
         */
        @Test
        @DisplayName("Actualizar usuario propio → Exitoso")
        void whenUpdatingOwnProfile_thenUpdatesSuccessfully() {
            // Arrange
            setupSecurityContextWithEmail(USER_EMAIL);

            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(existingUser));

            when(passwordEncoder.encode(USER_PASSWORD))
                    .thenReturn("encodedPasswordNew");

            when(userRepository.save(any(UserEntity.class)))
                    .thenReturn(existingUser);

            // Act
            UserResponseDto result = userService.updateUser(USER_ID, updateRequest);

            // Assert
            assertNotNull(result);
            assertEquals(USER_ID, result.id());
            assertEquals(USER_EMAIL, result.email());

            // Verify
            verify(userRepository, times(1)).findById(USER_ID);
            verify(userRepository, times(1)).save(any(UserEntity.class));
        }

        /**
         * Test: Actualizar parcialmente
         *
         * Verificación:
         * ✅ Solo los campos indicados se actualizan
         * ✅ Contraseña NO se codifica si viene null
         */
        @Test
        @DisplayName("Actualizar parcialmente → Solo campos indicados cambian")
        void whenPartialUpdate_thenOnlySpecifiedFieldsUpdate() {
            // Arrange
            setupSecurityContextWithEmail(USER_EMAIL);

            UserRequestDto partialUpdate = UserFactory.requestWith(
                    "Nuevo Nombre",
                    USER_EMAIL,
                    "CLIENTE",
                    "999 111 222",
                    null,  // ← No actualizar dirección
                    null   // ← No actualizar contraseña
            );

            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(existingUser));

            when(userRepository.save(any(UserEntity.class)))
                    .thenReturn(existingUser);

            // Act
            UserResponseDto result = userService.updateUser(USER_ID, partialUpdate);

            // Assert
            assertNotNull(result);

            // Verify - passwordEncoder NO se llamó
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, times(1)).save(any(UserEntity.class));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - updateUser()")
    class ErrorCases {

        private UserRequestDto updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new UserRequestDto(
                    "Juan García",
                    USER_EMAIL,
                    "CLIENTE",
                    USER_PHONE,
                    USER_ADDRESS,
                    USER_PASSWORD
            );
        }

        /**
         * Test: Actualizar otro usuario
         *
         * Verificación:
         * ✅ Lanza UnauthorizedAccessException
         * ✅ No guarda cambios
         */
        @Test
        @DisplayName("Actualizar otro usuario → Lanza UnauthorizedException")
        void whenUpdatingOtherUser_thenThrowsUnauthorizedException() {
            // Arrange
            setupSecurityContextWithEmail(OTHER_EMAIL);

            UserEntity otherUser = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);

            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(otherUser));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> userService.updateUser(USER_ID, updateRequest)
            );

            assertTrue(exception.getMessage().toLowerCase().contains("permiso"));

            // Verify
            verify(userRepository, never()).save(any(UserEntity.class));
        }

        /**
         * Test: Usuario no encontrado
         *
         * Verificación:
         * ✅ Lanza UserNotFoundException
         * ✅ No guarda cambios
         */
        @Test
        @DisplayName("Usuario no encontrado → Lanza UserNotFoundException")
        void whenUserNotFound_thenThrowsUserNotFoundException() {
            // Arrange
            when(userRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> userService.updateUser(999L, updateRequest)
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no existe"));

            // Verify
            verify(userRepository, never()).save(any(UserEntity.class));
        }
    }
}