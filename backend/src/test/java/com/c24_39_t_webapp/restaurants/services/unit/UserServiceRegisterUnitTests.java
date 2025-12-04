package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para UserService.createUser()
 *
 * ✅ Usuarios vienen de EntityModelFactory
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Crear usuario cliente exitoso
 * ✅ Crear usuario restaurante exitoso
 * ✅ Email duplicado → Lanza excepción
 * ✅ Rol inválido → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("UserService - createUser()")
class UserServiceRegisterUnitTests {

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "user@example.com";
    private static final String USER_PASSWORD = "Password123!";
    private static final String USER_PHONE = "555 666 777";
    private static final String USER_ADDRESS = "Calle Principal 123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.UserServiceImpl userService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - createUser()")
    class SuccessCases {

        private UserRequestDto validUserRequest;

        @BeforeEach
        void setUp() {
            // ✅ Crear request válido
            validUserRequest = UserFactory.requestWith(
                    "Juan García",
                    USER_EMAIL,
                    "CLIENTE",
                    USER_PHONE,
                    USER_ADDRESS,
                    USER_PASSWORD
            );
        }

        /**
         * Test: Crear usuario cliente nuevo
         *
         * Verificación:
         * ✅ Email no está duplicado
         * ✅ Rol es válido
         * ✅ Contraseña se codifica
         * ✅ Retorna UserResponseDto correcto
         */
        @Test
        @DisplayName("Crear usuario cliente → Exitoso")
        void whenCreatingClientUser_thenUserIsCreated() {
            // Arrange
            when(userRepository.existsByEmail(USER_EMAIL))
                    .thenReturn(false);

            when(passwordEncoder.encode(USER_PASSWORD))
                    .thenReturn("encodedPassword123");

            UserEntity savedUser = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            savedUser.setRole("CLIENTE");
            when(userRepository.save(any(UserEntity.class)))
                    .thenReturn(savedUser);

            // Act
            UserResponseDto result = userService.createUser(validUserRequest);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(USER_ID, result.id());
            assertEquals(USER_EMAIL, result.email());
            assertEquals("CLIENTE", result.role());

            // Verify
            verify(userRepository, times(1)).existsByEmail(USER_EMAIL);
            verify(passwordEncoder, times(1)).encode(USER_PASSWORD);
            verify(userRepository, times(1)).save(any(UserEntity.class));
        }

        /**
         * Test: Crear usuario restaurante
         *
         * Verificación:
         * ✅ Rol restaurante se guarda correctamente
         */
        @Test
        @DisplayName("Crear usuario restaurante → Exitoso")
        void whenCreatingRestaurantUser_thenUserIsCreated() {
            // Arrange
            UserRequestDto restaurantRequest = UserFactory.requestWith(
                    "Restaurant Owner",
                    "restaurant@example.com",
                    "RESTAURANTE",
                    USER_PHONE,
                    USER_ADDRESS,
                    USER_PASSWORD
            );
            when(userRepository.existsByEmail("restaurant@example.com"))
                    .thenReturn(false);

            when(passwordEncoder.encode(USER_PASSWORD))
                    .thenReturn("encodedPassword123");

            UserEntity savedUser = new UserEntity();
            savedUser.setId(USER_ID);
            savedUser.setEmail("restaurant@example.com");
            savedUser.setRole("restaurante");
            savedUser.setName("Restaurant Owner");

            when(userRepository.save(any(UserEntity.class)))
                    .thenReturn(savedUser);

            // Act
            UserResponseDto result = userService.createUser(restaurantRequest);

            // Assert
            assertEquals("restaurante", result.role());
            verify(userRepository, times(1)).save(any(UserEntity.class));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - createUser()")
    class ErrorCases {

        /**
         * Test: Email duplicado
         *
         * Verificación:
         * ✅ Lanza RuntimeException
         * ✅ No guarda el usuario
         */
        @Test
        @DisplayName("Email duplicado → Lanza excepción")
        void whenEmailAlreadyExists_thenThrowsException() {
            // Arrange
            UserRequestDto duplicateRequest = UserFactory.requestWith(
                    "Juan García",
                    USER_EMAIL,
                    "CLIENTE",
                    USER_PHONE,
                    USER_ADDRESS,
                    USER_PASSWORD
            );

            when(userRepository.existsByEmail(USER_EMAIL))
                    .thenReturn(true);

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> userService.createUser(duplicateRequest),
                    "Debe lanzar excepción por email duplicado"
            );

            // Verify
            verify(userRepository, times(1)).existsByEmail(USER_EMAIL);
            verify(userRepository, never()).save(any(UserEntity.class));
        }

        /**
         * Test: Rol inválido
         *
         * Verificación:
         * ✅ Lanza IllegalArgumentException
         * ✅ No guarda el usuario
         */
        @Test
        @DisplayName("Rol inválido → Lanza excepción")
        void whenInvalidRole_thenThrowsException() {
            // Arrange
            UserRequestDto invalidRoleRequest = UserFactory.requestWith(
                    "Juan García",
                    USER_EMAIL,
                    "admin",  // ← Rol inválido
                    USER_PHONE,
                    USER_ADDRESS,
                    USER_PASSWORD
            );

            when(userRepository.existsByEmail(USER_EMAIL))
                    .thenReturn(false);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> userService.createUser(invalidRoleRequest),
                    "Debe lanzar excepción por rol inválido"
            );

            // Verify
            verify(userRepository, never()).save(any(UserEntity.class));
        }
    }
}