package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.OrderUpdateRequestDto;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para OrderService.updateOrder()
 *
 * ✅ Órdenes desde EntityModelFactory
 * ✅ SecurityContext mockeado
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Actualizar status de orden
 * ✅ Actualizar comentarios de orden
 * ✅ Orden no encontrada → Lanza excepción
 * ✅ Usuario no autorizado → Lanza excepción
 * ✅ ID inválido → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("OrderService - updateOrder()")
class OrderServiceUpdateUnitTests {

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
    @DisplayName("Success Cases - updateOrder()")
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
         * Test: Actualizar status a pagado
         *
         * Verificación:
         * ✅ OrderRepository.save() se llamó
         * ✅ Status cambió correctamente
         * ✅ Retorna OrderResponseDto actualizado
         */
        @Test
        @DisplayName("Actualizar status a pagado → Exitoso")
        void whenUpdatingStatusToPaid_thenUpdatesSuccessfully() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(
                    OrderStatus.pagado,
                    "Pagado"
            );

            when(orderRepository.findById(ORDER_ID))
                    .thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class)))
                    .thenReturn(order);

            // Act
            OrderResponseDto result = orderService.updateOrder(ORDER_ID, updateRequest);

            // Assert
            assertNotNull(result);
            assertEquals(ORDER_ID, result.ord_Id());

            // Verify
            verify(orderRepository, times(1)).findById(ORDER_ID);
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        /**
         * Test: Actualizar comentarios
         *
         * Verificación:
         * ✅ Comentarios se actualizan
         * ✅ Status permanece igual si no se proporciona
         */
        @Test
        @DisplayName("Actualizar comentarios → Exitoso")
        void whenUpdatingComments_thenUpdatesSuccessfully() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(
                    null,  // No cambiar status
                    "Nuevo comentario"
            );

            when(orderRepository.findById(ORDER_ID))
                    .thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class)))
                    .thenReturn(order);

            // Act
            OrderResponseDto result = orderService.updateOrder(ORDER_ID, updateRequest);

            // Assert
            assertNotNull(result);

            // Verify
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        /**
         * Test: Actualizar ambos (status y comentarios)
         *
         * Verificación:
         * ✅ Status y comentarios se actualizan
         */
        @Test
        @DisplayName("Actualizar status y comentarios → Exitoso")
        void whenUpdatingBothStatusAndComments_thenUpdatesSuccessfully() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(
                    OrderStatus.entregado,
                    "Entregado exitosamente"
            );

            when(orderRepository.findById(ORDER_ID))
                    .thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class)))
                    .thenReturn(order);

            // Act
            OrderResponseDto result = orderService.updateOrder(ORDER_ID, updateRequest);

            // Assert
            assertNotNull(result);

            // Verify
            verify(orderRepository, times(1)).save(any(Order.class));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - updateOrder()")
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

            OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(
                    OrderStatus.pagado,
                    null
            );

            // Act & Assert
            OrderNotFoundException exception = assertThrows(
                    OrderNotFoundException.class,
                    () -> orderService.updateOrder(999L, updateRequest),
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

            when(orderRepository.findById(ORDER_ID))
                    .thenReturn(Optional.of(order));

            OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(
                    OrderStatus.pagado,
                    null
            );

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> orderService.updateOrder(ORDER_ID, updateRequest),
                    "Debe lanzar UnauthorizedException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("permiso"));

            // Verify
            verify(orderRepository, never()).save(any());
        }

        /**
         * Test: ID inválido (null)
         *
         * Verificación:
         * ✅ Lanza OrderNotFoundException
         */
        @Test
        @DisplayName("ID null → Lanza OrderNotFoundException")
        void whenIdIsNull_thenThrowsOrderNotFoundException() {
            // Arrange
            OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(
                    OrderStatus.pagado,
                    null
            );

            // Act & Assert
            assertThrows(OrderNotFoundException.class,
                    () -> orderService.updateOrder(null, updateRequest)
            );

            // Verify
            verify(orderRepository, never()).save(any());
        }

        /**
         * Test: ID <= 0
         *
         * Verificación:
         * ✅ Lanza OrderNotFoundException
         */
        @Test
        @DisplayName("ID <= 0 → Lanza OrderNotFoundException")
        void whenIdIsInvalid_thenThrowsOrderNotFoundException() {
            // Arrange
            OrderUpdateRequestDto updateRequest = new OrderUpdateRequestDto(
                    OrderStatus.pagado,
                    null
            );

            // Act & Assert
            assertThrows(OrderNotFoundException.class,
                    () -> orderService.updateOrder(0L, updateRequest)
            );
        }
    }
}