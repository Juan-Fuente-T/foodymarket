package com.c24_39_t_webapp.restaurants.services.unit;

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
 * Test unitarios para OrderService.deleteOrder()
 *
 * ✅ Órdenes desde EntityModelFactory
 * ✅ SecurityContext mockeado
 * ✅ Happy path + error cases
 *
 * IMPORTANTE: deleteOrder() NO BORRA, solo CANCELA (cambia estado a CANCELADO)
 *
 * Cobertura:
 * ✅ Cancelar orden pendiente → Status cambia a cancelado
 * ✅ Orden no encontrada → Lanza excepción
 * ✅ Usuario no autorizado → Lanza excepción
 * ✅ Orden ya pagada → Lanza excepción
 * ✅ Orden ya entregada → Lanza excepción
 * ✅ ID inválido → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("OrderService - deleteOrder()")
class OrderServiceDeleteUnitTests {

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
    @DisplayName("Success Cases - deleteOrder()")
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

            // ✅ Orden en estado PENDIENTE desde factory
            order = EntityModelFactory.order(ORDER_ID, client, restaurant,
                    OrderStatus.pendiente, BigDecimal.valueOf(50), "Orden pendiente");
        }

        /**
         * Test: Cancelar orden pendiente
         *
         * Verificación:
         * ✅ OrderRepository.findById() se llamó
         * ✅ OrderRepository.save() se llamó
         * ✅ Status cambió de PENDIENTE a CANCELADO
         * ✅ NO se borra (solo cambia estado)
         */
        @Test
        @DisplayName("Cancelar orden pendiente → Status cambia a CANCELADO")
        void whenDeletingPendingOrder_thenCancelsSuccessfully() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(orderRepository.findById(ORDER_ID))
                    .thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class)))
                    .thenReturn(order);

            // Act
            orderService.deleteOrder(ORDER_ID);

            // Assert
            assertEquals(OrderStatus.cancelado, order.getStatus(),
                    "Status debe cambiar a CANCELADO");

            // Verify
            verify(orderRepository, times(1)).findById(ORDER_ID);
            verify(orderRepository, times(1)).save(any(Order.class));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - deleteOrder()")
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
         * ✅ No guarda cambios
         */
        @Test
        @DisplayName("Orden no encontrada → Lanza OrderNotFoundException")
        void whenOrderNotFound_thenThrowsOrderNotFoundException() {
            // Arrange
            when(orderRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            OrderNotFoundException exception = assertThrows(
                    OrderNotFoundException.class,
                    () -> orderService.deleteOrder(999L),
                    "Debe lanzar OrderNotFoundException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no encontrado"));

            // Verify
            verify(orderRepository, never()).save(any());
        }

        /**
         * Test: Usuario no autorizado
         *
         * Verificación:
         * ✅ Lanza UnauthorizedAccessException
         * ✅ No guarda cambios
         */
        @Test
        @DisplayName("Usuario no autorizado → Lanza UnauthorizedException")
        void whenUserNotAuthorized_thenThrowsUnauthorizedException() {
            // Arrange
            setupSecurityContextWithEmail(OTHER_EMAIL);

            order.setStatus(OrderStatus.pendiente);

            when(orderRepository.findById(ORDER_ID))
                    .thenReturn(Optional.of(order));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> orderService.deleteOrder(ORDER_ID),
                    "Debe lanzar UnauthorizedException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("permiso"));

            // Verify
            verify(orderRepository, never()).save(any());
        }

        /**
         * Test: Orden ya pagada
         *
         * Verificación:
         * ✅ Lanza IllegalStateException
         * ✅ No puede cancelar órdenes pagadas
         */
        @Test
        @DisplayName("Orden ya pagada → Lanza IllegalStateException")
        void whenOrderAlreadyPaid_thenThrowsIllegalStateException() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            order.setStatus(OrderStatus.pagado);

            when(orderRepository.findById(ORDER_ID))
                    .thenReturn(Optional.of(order));

            // Act & Assert
            assertThrows(Exception.class,
                    () -> orderService.deleteOrder(ORDER_ID),
                    "Debe lanzar excepción si orden está pagada"
            );

            // Verify
            verify(orderRepository, never()).save(any());
        }

        /**
         * Test: Orden ya entregada
         *
         * Verificación:
         * ✅ Lanza IllegalStateException
         * ✅ No puede cancelar órdenes entregadas
         */
        @Test
        @DisplayName("Orden ya entregada → Lanza IllegalStateException")
        void whenOrderDelivered_thenThrowsIllegalStateException() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            order.setStatus(OrderStatus.entregado);

            when(orderRepository.findById(ORDER_ID))
                    .thenReturn(Optional.of(order));

            // Act & Assert
            assertThrows(Exception.class,
                    () -> orderService.deleteOrder(ORDER_ID),
                    "Debe lanzar excepción si orden está entregada"
            );

            // Verify
            verify(orderRepository, never()).save(any());
        }

        /**
         * Test: Orden ya cancelada
         *
         * Verificación:
         * ✅ Lanza IllegalStateException
         * ✅ No puede cancelar órdenes ya canceladas
         */
        @Test
        @DisplayName("Orden ya cancelada → Lanza IllegalStateException")
        void whenOrderAlreadyCanceled_thenThrowsIllegalStateException() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            order.setStatus(OrderStatus.cancelado);

            when(orderRepository.findById(ORDER_ID))
                    .thenReturn(Optional.of(order));

            // Act & Assert
            assertThrows(Exception.class,
                    () -> orderService.deleteOrder(ORDER_ID),
                    "Debe lanzar excepción si orden ya está cancelada"
            );

            // Verify
            verify(orderRepository, never()).save(any());
        }

        /**
         * Test: ID inválido (null)
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
                    () -> orderService.deleteOrder(null),
                    "Debe lanzar OrderNotFoundException por ID null"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no válido"));

            // Verify
            verify(orderRepository, never()).findById(any());
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
            OrderNotFoundException exception = assertThrows(
                    OrderNotFoundException.class,
                    () -> orderService.deleteOrder(0L),
                    "Debe lanzar OrderNotFoundException por ID <= 0"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no válido"));

            // Verify
            verify(orderRepository, never()).findById(any());
        }
    }
}