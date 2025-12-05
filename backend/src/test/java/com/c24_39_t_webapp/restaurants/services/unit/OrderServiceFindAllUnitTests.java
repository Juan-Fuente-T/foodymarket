package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.OrderResponseDto;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.models.Order;
import com.c24_39_t_webapp.restaurants.models.OrderStatus;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.OrderRepository;
import com.c24_39_t_webapp.restaurants.repository.RestaurantRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para OrderService.findAllOrders()
 *
 * ✅ Órdenes y restaurantes desde EntityModelFactory
 * ✅ SecurityContext mockeado
 * ✅ Happy path + edge cases + error cases
 *
 * Cobertura:
 * ✅ Obtener todas las órdenes de un restaurante
 * ✅ Lista vacía de órdenes
 * ✅ Restaurante no encontrado → Lanza excepción
 * ✅ Usuario no autorizado → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("OrderService - findAllOrders()")
class OrderServiceFindAllUnitTests {

    private static final Long RESTAURANT_ID = 1L;
    private static final Long OWNER_ID = 1L;
    private static final String OWNER_EMAIL = "owner@test.com";
    private static final String OTHER_EMAIL = "other@test.com";

    @Mock
    private RestaurantRepository restaurantRepository;

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
    @DisplayName("Success Cases - findAllOrders()")
    class SuccessCases {

        private Restaurant restaurant;
        private UserEntity owner;
        private UserEntity client;

        @BeforeEach
        void setUp() {
            // ✅ Owner desde factory
            owner = EntityModelFactory.restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);

            // ✅ Restaurante desde factory
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

            // ✅ Cliente desde factory
            client = EntityModelFactory.clientEntity(1L, "client@test.com");
        }

        /**
         * Test: Obtener todas las órdenes de un restaurante
         *
         * Verificación:
         * ✅ RestaurantRepository.findByIdAndUserEntityEmail() se llamó
         * ✅ OrderRepository.findByRestaurantId() se llamó
         * ✅ Retorna lista con órdenes
         * ✅ Size correcto
         */
        @Test
        @DisplayName("Obtener todas las órdenes → Retorna lista")
        void whenGettingAllOrders_thenReturnsList() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            // ✅ Órdenes desde factory
            Order order1 = EntityModelFactory.order(1L, client, restaurant,
                    OrderStatus.pendiente, BigDecimal.valueOf(50), "Order 1");
            Order order2 = EntityModelFactory.order(2L, client, restaurant,
                    OrderStatus.pagado, BigDecimal.valueOf(75), "Order 2");

            when(restaurantRepository.findByIdAndUserEntityEmail(RESTAURANT_ID, OWNER_EMAIL))
                    .thenReturn(Optional.of(restaurant));

            when(orderRepository.findByRestaurantId(restaurant))
                    .thenReturn(Arrays.asList(order1, order2));

            // Act
            List<OrderResponseDto> result = orderService.findAllOrders(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(2, result.size(), "Debe retornar 2 órdenes");
            assertEquals(1L, result.get(0).ord_Id(), "Primera orden debe ser ID 1");
            assertEquals(2L, result.get(1).ord_Id(), "Segunda orden debe ser ID 2");

            // Verify
            verify(restaurantRepository, times(1)).findByIdAndUserEntityEmail(RESTAURANT_ID, OWNER_EMAIL);
            verify(orderRepository, times(1)).findByRestaurantId(restaurant);
        }

        /**
         * Test: Una única orden
         *
         * Verificación:
         * ✅ Retorna lista con 1 orden
         */
        @Test
        @DisplayName("Una única orden → Retorna lista con 1 elemento")
        void whenOnlyOneOrder_thenReturnsListWithOneElement() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            Order singleOrder = EntityModelFactory.order(1L, client, restaurant);

            when(restaurantRepository.findByIdAndUserEntityEmail(RESTAURANT_ID, OWNER_EMAIL))
                    .thenReturn(Optional.of(restaurant));

            when(orderRepository.findByRestaurantId(restaurant))
                    .thenReturn(Arrays.asList(singleOrder));

            // Act
            List<OrderResponseDto> result = orderService.findAllOrders(RESTAURANT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size(), "Debe retornar 1 orden");

            // Verify
            verify(orderRepository, times(1)).findByRestaurantId(restaurant);
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases - findAllOrders()")
    class EdgeCases {

        private Restaurant restaurant;
        private UserEntity owner;

        @BeforeEach
        void setUp() {
            owner = EntityModelFactory.restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);
        }

        /**
         * Test: Lista vacía de órdenes
         *
         * Verificación:
         * ✅ Retorna Collections.emptyList()
         * ✅ No es null
         * ✅ Size es 0
         */
        @Test
        @DisplayName("Sin órdenes → Retorna lista vacía")
        void whenNoOrders_thenReturnsEmptyList() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.findByIdAndUserEntityEmail(RESTAURANT_ID, OWNER_EMAIL))
                    .thenReturn(Optional.of(restaurant));

            when(orderRepository.findByRestaurantId(restaurant))
                    .thenReturn(new ArrayList<>());

            // Act
            List<OrderResponseDto> result = orderService.findAllOrders(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertTrue(result.isEmpty(), "Lista debe estar vacía");
            assertEquals(0, result.size());

            // Verify
            verify(orderRepository, times(1)).findByRestaurantId(restaurant);
        }

        /**
         * Test: Muchas órdenes
         *
         * Verificación:
         * ✅ Retorna lista con muchos elementos
         */
        @Test
        @DisplayName("Muchas órdenes → Retorna lista completa")
        void whenManyOrders_thenReturnsCompleteList() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            UserEntity client = EntityModelFactory.clientEntity(1L, "client@test.com");
            List<Order> largeOrderList = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Order order = EntityModelFactory.order(
                        (long) i,
                        client,
                        restaurant,
                        OrderStatus.pendiente,
                        BigDecimal.valueOf(50 * i),
                        "Order " + i
                );
                largeOrderList.add(order);
            }

            when(restaurantRepository.findByIdAndUserEntityEmail(RESTAURANT_ID, OWNER_EMAIL))
                    .thenReturn(Optional.of(restaurant));

            when(orderRepository.findByRestaurantId(restaurant))
                    .thenReturn(largeOrderList);

            // Act
            List<OrderResponseDto> result = orderService.findAllOrders(RESTAURANT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(10, result.size(), "Debe retornar 10 órdenes");

            // Verify
            verify(orderRepository, times(1)).findByRestaurantId(restaurant);
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - findAllOrders()")
    class ErrorCases {

        /**
         * Test: Restaurante no encontrado
         *
         * Verificación:
         * ✅ Lanza RestaurantNotFoundException
         * ✅ No llama a OrderRepository
         */
        @Test
        @DisplayName("Restaurante no encontrado → Lanza RestaurantNotFoundException")
        void whenRestaurantNotFound_thenThrowsRestaurantNotFoundException() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.findByIdAndUserEntityEmail(999L, OWNER_EMAIL))
                    .thenReturn(Optional.empty());

            // Act & Assert
            RestaurantNotFoundException exception = assertThrows(
                    RestaurantNotFoundException.class,
                    () -> orderService.findAllOrders(999L),
                    "Debe lanzar RestaurantNotFoundException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no se encontró"));

            // Verify
            verify(orderRepository, never()).findByRestaurantId(any());
        }

        /**
         * Test: Usuario no autorizado (restaurante de otro user)
         *
         * Verificación:
         * ✅ Lanza RestaurantNotFoundException (porque no lo encuentra con ese email)
         */
        @Test
        @DisplayName("Usuario no autorizado → Lanza RestaurantNotFoundException")
        void whenUserNotAuthorized_thenThrowsRestaurantNotFoundException() {
            // Arrange
            setupSecurityContextWithEmail(OTHER_EMAIL);

            when(restaurantRepository.findByIdAndUserEntityEmail(RESTAURANT_ID, OTHER_EMAIL))
                    .thenReturn(Optional.empty());

            // Act & Assert
            RestaurantNotFoundException exception = assertThrows(
                    RestaurantNotFoundException.class,
                    () -> orderService.findAllOrders(RESTAURANT_ID)
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no se encontró"));

            // Verify
            verify(orderRepository, never()).findByRestaurantId(any());
        }
    }
}