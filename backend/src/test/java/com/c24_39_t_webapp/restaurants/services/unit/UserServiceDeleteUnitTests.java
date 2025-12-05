package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para UserService.deleteUser()
 *
 * ✅ Usuarios vienen de EntityModelFactory
 * ✅ SecurityContext mockeado
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Eliminar usuario propio
 * ✅ Intentar eliminar otro usuario → Lanza excepción
 * ✅ Usuario no encontrado → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("UserService - deleteUser()")
class UserServiceDeleteUnitTests {

    private static final Long USER_ID = 1L;
//    private static final Long OTHER_USER_ID = 2L;
    private static final String USER_EMAIL = "user@example.com";
    private static final String OTHER_EMAIL = "other@example.com";

    @Mock
    private UserRepository userRepository;

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
    @DisplayName("Success Cases - deleteUser()")
    class SuccessCases {

        private UserEntity existingUser;

        @BeforeEach
        void setUp() {
            // ✅ Usuario existente
            existingUser = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            existingUser.setEmail(USER_EMAIL);
        }

        /**
         * Test: Eliminar usuario propio
         *
         * Verificación:
         * ✅ Usuario autenticado es el propietario
         * ✅ UserRepository.deleteById() se llamó
         */
        @Test
        @DisplayName("Eliminar usuario propio → Exitoso")
        void whenDeletingOwnProfile_thenDeletesSuccessfully() {
            // Arrange
            setupSecurityContextWithEmail(USER_EMAIL);

            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(existingUser));

            // Act
            userService.deleteUser(USER_ID);

            // Verify
            verify(userRepository, times(1)).findById(USER_ID);
            verify(userRepository, times(1)).deleteById(USER_ID);
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - deleteUser()")
    class ErrorCases {

        private UserEntity existingUser;

        @BeforeEach
        void setUp() {
            // ✅ Usuario existente
            existingUser = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            existingUser.setEmail(USER_EMAIL);
        }

        /**
         * Test: Intentar eliminar otro usuario
         *
         * Verificación:
         * ✅ Lanza AccessDeniedException
         * ✅ No elimina nada
         */
        @Test
        @DisplayName("Eliminar otro usuario → Lanza AccessDeniedException")
        void whenDeletingOtherUser_thenThrowsAccessDeniedException() {
            // Arrange
            setupSecurityContextWithEmail(OTHER_EMAIL);

            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(existingUser));

            // Act & Assert
            AccessDeniedException exception = assertThrows(
                    AccessDeniedException.class,
                    () -> userService.deleteUser(USER_ID),
                    "Debe lanzar AccessDeniedException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("permiso"));

            // Verify
            verify(userRepository, never()).deleteById(any());
        }

        /**
         * Test: Usuario no encontrado
         *
         * Verificación:
         * ✅ Lanza UserNotFoundException
         * ✅ No elimina nada
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
                    () -> userService.deleteUser(999L),
                    "Debe lanzar UserNotFoundException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no ha sido encontrado"));

            // Verify
            verify(userRepository, never()).deleteById(any());
        }
    }
}