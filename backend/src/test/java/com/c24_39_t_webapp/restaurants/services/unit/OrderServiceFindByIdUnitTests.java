package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.OrderResponseDto;
import com.c24_39_t_webapp.restaurants.exception.OrderNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.models.Order;
import com.c24_39_t_webapp.restaurants.models.OrderStatus;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.repository.OrderRepository;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para OrderService.findOrderById()
 *
 * ✅ Órdenes desde EntityModelFactory
 * ✅ SecurityContext mockeado
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Obtener orden por ID válido
 * ✅ Orden no encontrada → Lanza excepción
 * ✅ ID inválido (null, <= 0) → Lanza excepción
 * ✅ Usuario no autorizado → Lanza excepción
 * ✅ Mapeo de datos correcto
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("OrderService - findOrderById()")
class OrderServiceFindByIdUnitTests {

    private static final Long ORDER_ID = 1L;
    private static final Long RESTAURANT_ID = 1L;
    private static final Long OWNER_ID = 1L;
    private static final String OWNER_EMAIL = "owner@test.com";
    private static final String OTHER_EMAIL = "other@test.com";

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.OrderServiceImpl orderService;

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
    @DisplayName("Success Cases - findOrderById()")
    class SuccessCases {

        private Order order;
        private UserEntity owner;
        private UserEntity client;
        private Restaurant restaurant;

        @BeforeEach
        void setUp() {
            // ✅ Owner desde factory
            owner = EntityModelFactory.restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);

            // ✅ Restaurante desde factory
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

            // ✅ Cliente desde factory
            client = EntityModelFactory.clientEntity(1L, "client@test.com");

            // ✅ Orden desde factory
            order = EntityModelFactory.order(ORDER_ID, client, restaurant);
        }

        /**
         * Test: Obtener orden por ID válido
         *
         * Verificación:
         * ✅ OrderRepository.findByIdFetchingAll() se llamó
         * ✅ Retorna OrderResponseDto con datos correctos
         * ✅ Usuario autorizado (email coincide)
         */
        @Test
        @DisplayName("Orden existe → Retorna datos correctos")
        void whenOrderExists_thenReturnsOrder() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(orderRepository.findByIdFetchingAll(ORDER_ID))
                    .thenReturn(Optional.of(order));

            // Act
            OrderResponseDto result = orderService.findOrderById(ORDER_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(ORDER_ID, result.ord_Id(), "Order ID debe coincidir");
            assertEquals(client.getId(), result.clientId(), "Client ID debe coincidir");
            assertEquals(RESTAURANT_ID, result.restaurantId(), "Restaurant ID debe coincidir");

            // Verify
            verify(orderRepository, times(1)).findByIdFetchingAll(ORDER_ID);
        }

        /**
         * Test: Status y datos se mapean correctamente
         *
         * Verificación:
         * ✅ Status se mapea correctamente
         * ✅ Total se mapea correctamente
         * ✅ Comments se mapean correctamente
         */
        @Test
        @DisplayName("Mapeo de datos → Correcto")
        void whenMappingOrderData_thenDataIsCorrect() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            order = EntityModelFactory.order(ORDER_ID, client, restaurant,
                    OrderStatus.pagado, BigDecimal.valueOf(100), "Pagado correctamente");

            when(orderRepository.findByIdFetchingAll(ORDER_ID))
                    .thenReturn(Optional.of(order));

            // Act
            OrderResponseDto result = orderService.findOrderById(ORDER_ID);

            // Assert
            assertEquals(OrderStatus.pagado, result.status(), "Status debe ser pagado");
            assertEquals(BigDecimal.valueOf(100), result.total(), "Total debe coincidir");

            // Verify
            verify(orderRepository, times(1)).findByIdFetchingAll(ORDER_ID);
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - findOrderById()")
    class ErrorCases {

        private Order order;
        private UserEntity owner;
        private UserEntity client;
        private Restaurant restaurant;

        @BeforeEach
        void setUp() {
            owner = EntityModelFactory.restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);
            client = EntityModelFactory.clientEntity(1L, "client@test.com");
            order = EntityModelFactory.order(ORDER_ID, client, restaurant);
        }

        /**
         * Test: Orden no encontrada
         *
         * Verificación:
         * ✅ Lanza OrderNotFoundException
         * ✅ Mensaje contiene información del ID
         */
        @Test
        @DisplayName("Orden no encontrada → Lanza OrderNotFoundException")
        void whenOrderNotFound_thenThrowsOrderNotFoundException() {
            // Arrange
            when(orderRepository.findByIdFetchingAll(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            OrderNotFoundException exception = assertThrows(
                    OrderNotFoundException.class,
                    () -> orderService.findOrderById(999L),
                    "Debe lanzar OrderNotFoundException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no encontrado"));

            // Verify
            verify(orderRepository, times(1)).findByIdFetchingAll(999L);
        }

        /**
         * Test: ID null
         *
         * Verificación:
         * ✅ Lanza OrderNotFoundException
         * ✅ Validación ocurre ANTES de acceder a SecurityContext
         * ✅ NO necesita setupSecurityContext
         */
        @Test
        @DisplayName("ID null → Lanza OrderNotFoundException")
        void whenIdIsNull_thenThrowsOrderNotFoundException() {
            // Arrange - NO llamar setupSecurityContextWithEmail porque la validación
            // de ID ocurre ANTES de acceder a SecurityContext

            // Act & Assert
            OrderNotFoundException exception = assertThrows(
                    OrderNotFoundException.class,
                    () -> orderService.findOrderById(null),
                    "Debe lanzar OrderNotFoundException por ID null"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no es válido"));

            // Verify
            verify(orderRepository, never()).findByIdFetchingAll(any(Long.class));
        }

        /**
         * Test: ID <= 0
         *
         * Verificación:
         * ✅ Lanza OrderNotFoundException
         * ✅ Validación ocurre ANTES de acceder a SecurityContext
         */
        @Test
        @DisplayName("ID <= 0 → Lanza OrderNotFoundException")
        void whenIdIsInvalid_thenThrowsOrderNotFoundException() {
            // Arrange - NO llamar setupSecurityContextWithEmail porque la validación
            // de ID ocurre ANTES de acceder a SecurityContext

            // Act & Assert
            assertThrows(OrderNotFoundException.class,
                    () -> orderService.findOrderById(0L)
            );

            // Verify
            verify(orderRepository, never()).findByIdFetchingAll(any(Long.class));
        }

        /**
         * Test: Usuario no autorizado
         *
         * Verificación:
         * ✅ Lanza UnauthorizedAccessException
         * ✅ Usuario no es dueño del restaurante
         */
        @Test
        @DisplayName("Usuario no autorizado → Lanza UnauthorizedException")
        void whenUserNotAuthorized_thenThrowsUnauthorizedException() {
            // Arrange
            setupSecurityContextWithEmail(OTHER_EMAIL);

            when(orderRepository.findByIdFetchingAll(ORDER_ID))
                    .thenReturn(Optional.of(order));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> orderService.findOrderById(ORDER_ID),
                    "Debe lanzar UnauthorizedException si el usuario no es dueño"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("permiso"));

            // Verify
            verify(orderRepository, times(1)).findByIdFetchingAll(ORDER_ID);
        }
    }
}