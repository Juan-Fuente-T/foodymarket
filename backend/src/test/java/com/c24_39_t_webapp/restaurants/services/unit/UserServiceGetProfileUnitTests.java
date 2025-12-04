package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para UserService.getUserProfile()
 *
 * ✅ Usuarios vienen de EntityModelFactory
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Obtener perfil existente
 * ✅ Usuario no encontrado → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("UserService - getUserProfile()")
class UserServiceGetProfileUnitTests {

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "user@example.com";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.UserServiceImpl userService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - getUserProfile()")
    class SuccessCases {

        private UserEntity existingUser;

        @BeforeEach
        void setUp() {
            // ✅ Usuario existente
            existingUser = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
        }

        /**
         * Test: Obtener perfil existente
         *
         * Verificación:
         * ✅ UserRepository.findByEmail() se llamó
         * ✅ Retorna UserResponseDto con datos correctos
         */
        @Test
        @DisplayName("Obtener perfil existente → Retorna datos correctos")
        void whenGettingExistingProfile_thenReturnsProfile() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL))
                    .thenReturn(Optional.of(existingUser));

            // Act
            UserResponseDto result = userService.getUserProfile(USER_EMAIL);

            // Assert
            assertNotNull(result);
            assertEquals(USER_ID, result.id());
            assertEquals(USER_EMAIL, result.email());
            assertNotNull(result.name());

            // Verify
            verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - getUserProfile()")
    class ErrorCases {

        /**
         * Test: Usuario no encontrado
         *
         * Verificación:
         * ✅ Lanza UserNotFoundException
         */
        @Test
        @DisplayName("Usuario no encontrado → Lanza UserNotFoundException")
        void whenUserNotFound_thenThrowsUserNotFoundException() {
            // Arrange
            when(userRepository.findByEmail("noexiste@example.com"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> userService.getUserProfile("noexiste@example.com"),
                    "Debe lanzar UserNotFoundException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no existe"));

            // Verify
            verify(userRepository, times(1)).findByEmail("noexiste@example.com");
        }
    }
}