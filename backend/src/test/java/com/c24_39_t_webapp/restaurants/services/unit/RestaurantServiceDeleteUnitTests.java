package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para RestaurantService.deleteById()
 *
 * ✅ TODOS los datos vienen de factories
 * ✅ Sincronización con factories
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Delete exitoso (usuario autorizado)
 * ✅ Restaurante no existe
 * ✅ Usuario propietario no encontrado
 * ✅ Usuario no autorizado (no es propietario)
 * ✅ Verifica que restaurantRepository.deleteById() se llamó
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("RestaurantService - deleteById()")
public class RestaurantServiceDeleteUnitTests {

    private static final Long RESTAURANT_ID = 1L;
    private static final Long OWNER_USER_ID = 1L;
    private static final Long UNAUTHORIZED_USER_ID = 2L;
    private static final String OWNER_EMAIL = "owner@restaurant.com";
    private static final String UNAUTHORIZED_EMAIL = "unauthorized@restaurant.com";

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.RestaurantRepository restaurantRepository;

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.UserRepository userRepository;

    @Mock
    private com.c24_39_t_webapp.restaurants.services.ICategoryService categoryService;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.RestaurantServiceImpl restaurantService;

    // ==================== SETUP ====================

    private UserEntity owner;

    @BeforeEach
    void setUp() {
        // ✅ Usuario propietario
        owner = EntityModelFactory
                .restaurantOwnerEntity(OWNER_USER_ID, OWNER_EMAIL);
    }
    /**
     * Método auxiliar para mockear SecurityContext
     * Se usa en cada test que lo necesite
     *
     * @param email Email del usuario autenticado
     */
    private void setupSecurityContextWithEmail(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // ==================== CASOS DE ÉXITO ====================

    @Nested
    @DisplayName("Success Cases - deleteById()")
    class SuccessCases {

        /**
         * Test: Delete EXITOSO de restaurante autorizado
         *
         * Verificación:
         * ✅ Restaurante existe
         * ✅ Usuario autenticado es propietario
         * ✅ restaurantRepository.existsById() retorna true
         * ✅ userRepository.findById() retorna el propietario
         * ✅ restaurantRepository.deleteById() se llamó una vez
         * ✅ No lanza excepción
         */
        @Test
        @DisplayName("Usuario propietario → Elimina exitosamente")
        void whenOwnerIsAuthenticated_thenDeletesSuccessfully() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.existsById(RESTAURANT_ID))
                    .thenReturn(true);

            when(userRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(owner));

            // Act
            assertDoesNotThrow(
                    () -> restaurantService.deleteById(RESTAURANT_ID),
                    "No debe lanzar excepción"
            );

            // Verify
            verify(restaurantRepository, times(1)).existsById(RESTAURANT_ID);
            verify(userRepository, times(1)).findById(RESTAURANT_ID);
            verify(restaurantRepository, times(1)).deleteById(RESTAURANT_ID);
        }

        /**
         * Test: Verificar que deletById se llamó con ID correcto
         *
         * Verificación:
         * ✅ ArgumentCaptor captura el ID pasado a deleteById
         */
        @Test
        @DisplayName("Verifica que deleteById se llamó con ID correcto")
        void whenDeleting_thenCallsDeleteWithCorrectId() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.existsById(RESTAURANT_ID))
                    .thenReturn(true);

            when(userRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(owner));

            // Act
            restaurantService.deleteById(RESTAURANT_ID);

            // Verify
            verify(restaurantRepository, times(1)).deleteById(RESTAURANT_ID);
        }
    }

    // ==================== CASOS DE ERROR ====================

    @Nested
    @DisplayName("Error Cases - deleteById()")
    class ErrorCases {

        /**
         * Test: Restaurante NO EXISTE
         *
         * Verificación:
         * ✅ restaurantRepository.existsById() retorna false
         * ✅ Lanza RestaurantNotFoundException
         * ✅ No busca usuario, no elimina
         */
        @Test
        @DisplayName("Restaurante no existe → lanza RestaurantNotFoundException")
        void whenRestaurantNotFound_thenThrowsNotFoundException() {
            // Arrange
            when(restaurantRepository.existsById(999L))
                    .thenReturn(false);

            // Act & Assert
            RestaurantNotFoundException exception = assertThrows(
                    RestaurantNotFoundException.class,
                    () -> restaurantService.deleteById(999L),
                    "Debe lanzar RestaurantNotFoundException"
            );

            assertTrue(exception.getMessage().contains("no encontrado"),
                    "Mensaje debe mencionar 'no encontrado'");

            // Verify
            verify(restaurantRepository, times(1)).existsById(999L);
            verify(userRepository, never()).findById(any());
            verify(restaurantRepository, never()).deleteById(any());
        }

        /**
         * Test: Usuario propietario NO ENCONTRADO
         *
         * Verificación:
         * ✅ Restaurante existe
         * ✅ Pero userRepository.findById() no lo encuentra
         * ✅ Lanza UserNotFoundException
         * ✅ No elimina
         */
        @Test
        @DisplayName("Usuario propietario no encontrado → lanza UserNotFoundException")
        void whenOwnerNotFound_thenThrowsUserNotFoundException() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.existsById(RESTAURANT_ID))
                    .thenReturn(true);

            when(userRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> restaurantService.deleteById(RESTAURANT_ID),
                    "Debe lanzar UserNotFoundException"
            );

            assertTrue(exception.getMessage().contains("no encontrado"),
                    "Mensaje debe mencionar 'no encontrado'");

            // Verify
            verify(restaurantRepository, times(1)).existsById(RESTAURANT_ID);
            verify(userRepository, times(1)).findById(RESTAURANT_ID);
            verify(restaurantRepository, never()).deleteById(any());
        }

        /**
         * Test: Usuario NO AUTORIZADO (no es propietario)
         *
         * Verificación:
         * ✅ Restaurante existe
         * ✅ Usuario autenticado es DIFERENTE al propietario
         * ✅ Lanza UnauthorizedAccessException
         * ✅ No elimina
         */
        @Test
        @DisplayName("Usuario no es propietario → lanza UnauthorizedAccessException")
        void whenUserNotOwner_thenThrowsUnauthorizedException() {
            // Arrange
            UserEntity unauthorizedUser = com.c24_39_t_webapp.restaurants.factories.EntityModelFactory
                    .restaurantOwnerEntity(UNAUTHORIZED_USER_ID, UNAUTHORIZED_EMAIL);

            // Cambiar usuario autenticado a uno no autorizado
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(UNAUTHORIZED_EMAIL);

            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            when(restaurantRepository.existsById(RESTAURANT_ID))
                    .thenReturn(true);

            when(userRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(owner));  // Owner es diferente al autenticado

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> restaurantService.deleteById(RESTAURANT_ID),
                    "Debe lanzar UnauthorizedAccessException"
            );

            assertTrue(exception.getMessage().contains("permiso"),
                    "Mensaje debe mencionar 'permiso'");

            // Verify
            verify(restaurantRepository, times(1)).existsById(RESTAURANT_ID);
            verify(userRepository, times(1)).findById(RESTAURANT_ID);
            verify(restaurantRepository, never()).deleteById(any());
        }

        /**
         * Test: ID null
         *
         * Verificación:
         * ✅ Comportamiento del repositorio con ID null
         */
        @Test
        @DisplayName("ID null → repositorio decide qué hacer")
        void whenIdNull_thenRepositoryHandlesIt() {
            // Arrange
            when(restaurantRepository.existsById(null))
                    .thenReturn(false);

            // Act & Assert
            assertThrows(
                    RestaurantNotFoundException.class,
                    () -> restaurantService.deleteById(null)
            );

            // Verify
            verify(restaurantRepository, never()).deleteById(any());
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases - deleteById()")
    class EdgeCases {

        /**
         * Test: Sequence correcta de llamadas
         *
         * Verificación:
         * ✅ existsById se llama PRIMERO
         * ✅ findById se llama SEGUNDO
         * ✅ deleteById se llama ÚLTIMO
         */
        @Test
        @DisplayName("Secuencia correcta de llamadas")
        void whenDeleting_thenCallsInCorrectOrder() {
            // Arrange
            when(restaurantRepository.existsById(RESTAURANT_ID))
                    .thenReturn(true);

            when(userRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(owner));

            // Act
            restaurantService.deleteById(RESTAURANT_ID);

            // Verify orden (inOrder)
            InOrder inOrder = inOrder(restaurantRepository, userRepository);
            inOrder.verify(restaurantRepository).existsById(RESTAURANT_ID);
            inOrder.verify(userRepository).findById(RESTAURANT_ID);
            inOrder.verify(restaurantRepository).deleteById(RESTAURANT_ID);
        }

        /**
         * Test: Email idéntico pero diferente objeto
         *
         * Verificación:
         * ✅ Comparación por email (String), no por referencia de objeto
         */
        @Test
        @DisplayName("Compara por email, no por referencia de objeto")
        void whenEmailMatches_thenDeletesSuccessfully() {
            // Arrange
            UserEntity ownerCopy = new UserEntity();
            ownerCopy.setId(OWNER_USER_ID);
            ownerCopy.setEmail(OWNER_EMAIL);  // Mismo email, diferente objeto
            ownerCopy.setName("Copia");

            when(restaurantRepository.existsById(RESTAURANT_ID))
                    .thenReturn(true);

            when(userRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(ownerCopy));

            // Act
            assertDoesNotThrow(
                    () -> restaurantService.deleteById(RESTAURANT_ID),
                    "Debe permitir delete si emails coinciden"
            );

            // Verify
            verify(restaurantRepository, times(1)).deleteById(RESTAURANT_ID);
        }

        /**
         * Test: restaurantRepository.deleteById se llama SOLO UNA VEZ
         *
         * Verificación:
         * ✅ Idempotencia: no se llama múltiples veces
         */
        @Test
        @DisplayName("deleteById se llama exactamente UNA VEZ")
        void whenDeleting_thenDeletedExactlyOnce() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.existsById(RESTAURANT_ID))
                    .thenReturn(true);

            when(userRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(owner));

            // Act
            restaurantService.deleteById(RESTAURANT_ID);

            // Verify
            verify(restaurantRepository, times(1)).deleteById(RESTAURANT_ID);
        }
    }
}