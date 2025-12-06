package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.OrderResponseDto;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.models.Order;
import com.c24_39_t_webapp.restaurants.models.OrderStatus;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.OrderRepository;
import com.c24_39_t_webapp.restaurants.repository.RestaurantRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para OrderService métodos por restaurante
 *
 * ✅ findByCreatedAtBetween(Long restaurantId, LocalDateTime start, LocalDateTime end)
 * ✅ findByStatusAndRestaurantId(OrderStatus status, Long restaurantId)
 * ✅ findAllOrdersByOwnerId(Long ownerId)
 *
 * ✅ Órdenes, restaurantes y users desde EntityModelFactory
 * ✅ SecurityContext mockeado
 * ✅ Happy path + error cases + edge cases
 *
 * Cobertura:
 * ✅ Obtener órdenes por rango de fechas
 * ✅ Obtener órdenes por estado
 * ✅ Obtener todas las órdenes de un owner
 * ✅ Usuario no autorizado → Lanza excepción
 * ✅ Restaurante no encontrado → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("OrderService - Métodos por Restaurante (findByCreatedAtBetween, findByStatusAndRestaurantId, findAllOrdersByOwnerId)")
class OrderServiceFindByRestaurantUnitTests {

    private static final Long RESTAURANT_ID = 1L;
    private static final Long OWNER_ID = 1L;
    private static final String OWNER_EMAIL = "owner@test.com";
    private static final String OTHER_EMAIL = "other@test.com";

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

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

    // ==================== findByCreatedAtBetween() ====================

    @Nested
    @DisplayName("findByCreatedAtBetween() - Success Cases")
    class FindByCreatedAtBetweenSuccessCases {

        private Restaurant restaurant;
        private UserEntity owner;
        private UserEntity client;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<Order> orders;

        @BeforeEach
        void setUp() {
            owner = EntityModelFactory.restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);
            client = EntityModelFactory.clientEntity(1L, "client@test.com");

            startDate = LocalDateTime.now().minusDays(7);
            endDate = LocalDateTime.now();

            orders = Arrays.asList(
                    EntityModelFactory.order(1L, client, restaurant, OrderStatus.pendiente, BigDecimal.valueOf(50), "Orden 1"),
                    EntityModelFactory.order(2L, client, restaurant, OrderStatus.pagado, BigDecimal.valueOf(75), "Orden 2")
            );
        }

        /**
         * Test: Obtener órdenes entre fechas
         *
         * Verificación:
         * ✅ RestaurantRepository.findByIdAndUserEntityEmail() se llamó
         * ✅ OrderRepository.findByRestaurantIdAndCreatedAtBetween() se llamó
         * ✅ Retorna lista con órdenes en el rango
         */
        @Test
        @DisplayName("Obtener órdenes entre fechas → Retorna lista")
        void whenGettingOrdersBetweenDates_thenReturnsList() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.findByIdAndUserEntityEmail(RESTAURANT_ID, OWNER_EMAIL))
                    .thenReturn(Optional.of(restaurant));
            when(orderRepository.findByRestaurantIdAndCreatedAtBetween(restaurant, startDate, endDate))
                    .thenReturn(orders);

            // Act
            List<OrderResponseDto> result = orderService.findByCreatedAtBetween(RESTAURANT_ID, startDate, endDate);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size(), "Debe retornar órdenes en el rango");

            // Verify
            verify(restaurantRepository, times(1)).findByIdAndUserEntityEmail(RESTAURANT_ID, OWNER_EMAIL);
            verify(orderRepository, times(1)).findByRestaurantIdAndCreatedAtBetween(restaurant, startDate, endDate);
        }

        /**
         * Test: Sin órdenes en el rango
         *
         * Verificación:
         * ✅ Retorna lista vacía
         */
        @Test
        @DisplayName("Sin órdenes en el rango → Retorna lista vacía")
        void whenNoOrdersInRange_thenReturnsEmptyList() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.findByIdAndUserEntityEmail(RESTAURANT_ID, OWNER_EMAIL))
                    .thenReturn(Optional.of(restaurant));
            when(orderRepository.findByRestaurantIdAndCreatedAtBetween(restaurant, startDate, endDate))
                    .thenReturn(new ArrayList<>());

            // Act
            List<OrderResponseDto> result = orderService.findByCreatedAtBetween(RESTAURANT_ID, startDate, endDate);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByCreatedAtBetween() - Error Cases")
    class FindByCreatedAtBetweenErrorCases {

        private LocalDateTime startDate;
        private LocalDateTime endDate;

        @BeforeEach
        void setUp() {
            startDate = LocalDateTime.now().minusDays(7);
            endDate = LocalDateTime.now();
        }

        /**
         * Test: Usuario no autorizado
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
                    () -> orderService.findByCreatedAtBetween(RESTAURANT_ID, startDate, endDate)
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("no se encontró"),
                    "Mensaje: " + exception.getMessage()
            );

            // Verify
            verify(orderRepository, never()).findByRestaurantIdAndCreatedAtBetween(any(), any(), any());
        }
    }

    // ==================== findByStatusAndRestaurantId() ====================

    @Nested
    @DisplayName("findByStatusAndRestaurantId() - Success Cases")
    class FindByStatusAndRestaurantIdSuccessCases {

        private Restaurant restaurant;
        private UserEntity owner;
        private UserEntity client;
        private List<Order> orders;

        @BeforeEach
        void setUp() {
            owner = EntityModelFactory.restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);
            client = EntityModelFactory.clientEntity(1L, "client@test.com");

            orders = Arrays.asList(
                    EntityModelFactory.order(1L, client, restaurant, OrderStatus.pendiente, BigDecimal.valueOf(50), "Orden 1"),
                    EntityModelFactory.order(2L, client, restaurant, OrderStatus.pendiente, BigDecimal.valueOf(75), "Orden 2")
            );
        }

        /**
         * Test: Obtener órdenes por status
         *
         * Verificación:
         * ✅ OrderRepository.findByStatusAndRestaurantId_Id() se llamó
         * ✅ Retorna lista con órdenes en el status especificado
         */
        @Test
        @DisplayName("Obtener órdenes por status → Retorna lista")
        void whenGettingOrdersByStatus_thenReturnsList() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.findByIdAndUserEntityEmail(RESTAURANT_ID, OWNER_EMAIL))
                    .thenReturn(Optional.of(restaurant));
            when(orderRepository.findByStatusAndRestaurantId_Id(OrderStatus.pendiente, RESTAURANT_ID))
                    .thenReturn(orders);

            // Act
            List<OrderResponseDto> result = orderService.findByStatusAndRestaurantId(OrderStatus.pendiente, RESTAURANT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size(), "Debe retornar órdenes pendientes");

            // Verify
            verify(orderRepository, times(1)).findByStatusAndRestaurantId_Id(OrderStatus.pendiente, RESTAURANT_ID);
        }

        /**
         * Test: Sin órdenes con ese status
         *
         * Verificación:
         * ✅ Retorna lista vacía
         */
        @Test
        @DisplayName("Sin órdenes en ese status → Retorna lista vacía")
        void whenNoOrdersWithStatus_thenReturnsEmptyList() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.findByIdAndUserEntityEmail(RESTAURANT_ID, OWNER_EMAIL))
                    .thenReturn(Optional.of(restaurant));
            when(orderRepository.findByStatusAndRestaurantId_Id(OrderStatus.entregado, RESTAURANT_ID))
                    .thenReturn(new ArrayList<>());

            // Act
            List<OrderResponseDto> result = orderService.findByStatusAndRestaurantId(OrderStatus.entregado, RESTAURANT_ID);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ==================== findAllOrdersByOwnerId() ====================

    @Nested
    @DisplayName("findAllOrdersByOwnerId() - Success Cases")
    class FindAllOrdersByOwnerIdSuccessCases {

        private UserEntity owner;
        private UserEntity client;
        private Restaurant restaurant;
        private List<Order> orders;

        @BeforeEach
        void setUp() {
            owner = EntityModelFactory.restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);
            client = EntityModelFactory.clientEntity(1L, "client@test.com");
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

            orders = Arrays.asList(
                    EntityModelFactory.order(1L, client, restaurant, OrderStatus.pendiente, BigDecimal.valueOf(50), "Orden 1"),
                    EntityModelFactory.order(2L, client, restaurant, OrderStatus.pagado, BigDecimal.valueOf(75), "Orden 2")
            );
        }

        /**
         * Test: Obtener todas las órdenes del owner
         *
         * Verificación:
         * ✅ RestaurantRepository.findRestaurantIdsByOwnerId() se llamó
         * ✅ OrderRepository.findByRestaurantId_IdInWithDetails() se llamó
         * ✅ Retorna todas las órdenes de los restaurantes del owner
         */
        @Test
        @DisplayName("Obtener todas las órdenes del owner → Retorna lista")
        void whenGettingAllOrdersByOwner_thenReturnsList() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));
            when(restaurantRepository.findRestaurantIdsByOwnerId(OWNER_ID))
                    .thenReturn(Arrays.asList(RESTAURANT_ID));
            when(orderRepository.findByRestaurantId_IdInWithDetails(Arrays.asList(RESTAURANT_ID)))
                    .thenReturn(orders);

            // Act
            List<OrderResponseDto> result = orderService.findAllOrdersByOwnerId(OWNER_ID);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size(), "Debe retornar todas las órdenes del owner");

            // Verify
            verify(userRepository, times(1)).findById(OWNER_ID);
            verify(restaurantRepository, times(1)).findRestaurantIdsByOwnerId(OWNER_ID);
            verify(orderRepository, times(1)).findByRestaurantId_IdInWithDetails(Arrays.asList(RESTAURANT_ID));
        }

        /**
         * Test: Owner sin restaurantes
         *
         * Verificación:
         * ✅ Retorna lista vacía
         */
        @Test
        @DisplayName("Owner sin restaurantes → Retorna lista vacía")
        void whenOwnerHasNoRestaurants_thenReturnsEmptyList() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));
            when(restaurantRepository.findRestaurantIdsByOwnerId(OWNER_ID))
                    .thenReturn(new ArrayList<>());

            // Act
            List<OrderResponseDto> result = orderService.findAllOrdersByOwnerId(OWNER_ID);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty(), "Debe retornar lista vacía");

            // Verify
            verify(orderRepository, never()).findByRestaurantId_IdInWithDetails(any());
        }

        /**
         * Test: Owner con múltiples restaurantes
         *
         * Verificación:
         * ✅ Retorna órdenes de todos los restaurantes del owner
         */
        @Test
        @DisplayName("Owner con múltiples restaurantes → Retorna órdenes de todos")
        void whenOwnerHasMultipleRestaurants_thenReturnsAllOrders() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            UserEntity client2 = EntityModelFactory.clientEntity(2L, "client2@test.com");
            Restaurant restaurant2 = EntityModelFactory.restaurant(2L, owner);

            List<Long> restaurantIds = Arrays.asList(RESTAURANT_ID, 2L);
            List<Order> allOrders = Arrays.asList(
                    EntityModelFactory.order(1L, client, restaurant, OrderStatus.pendiente, BigDecimal.valueOf(50), "Orden 1"),
                    EntityModelFactory.order(2L, client2, restaurant2, OrderStatus.pagado, BigDecimal.valueOf(75), "Orden 2")
            );

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));
            when(restaurantRepository.findRestaurantIdsByOwnerId(OWNER_ID))
                    .thenReturn(restaurantIds);
            when(orderRepository.findByRestaurantId_IdInWithDetails(restaurantIds))
                    .thenReturn(allOrders);

            // Act
            List<OrderResponseDto> result = orderService.findAllOrdersByOwnerId(OWNER_ID);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size(), "Debe retornar órdenes de todos los restaurantes");
        }
    }

    @Nested
    @DisplayName("findAllOrdersByOwnerId() - Error Cases")
    class FindAllOrdersByOwnerIdErrorCases {

        /**
         * Test: Owner no encontrado
         *
         * Verificación:
         * ✅ Lanza UserNotFoundException
         */
        @Test
        @DisplayName("Owner no encontrado → Lanza UserNotFoundException")
        void whenOwnerNotFound_thenThrowsUserNotFoundException() {
            when(userRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> orderService.findAllOrdersByOwnerId(999L)
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("no encontrado"),
                    "Mensaje: " + exception.getMessage()
            );

            // Verify
            verify(restaurantRepository, never()).findRestaurantIdsByOwnerId(any());
        }

        /**
         * Test: Usuario no autorizado
         *
         * Verificación:
         * ✅ Lanza UnauthorizedAccessException
         * ✅ Email no coincide
         */
        @Test
        @DisplayName("Usuario no autorizado → Lanza UnauthorizedException")
        void whenUserNotAuthorized_thenThrowsUnauthorizedException() {
            // Arrange
            setupSecurityContextWithEmail(OTHER_EMAIL);

            UserEntity owner = EntityModelFactory.restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> orderService.findAllOrdersByOwnerId(OWNER_ID)
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("permiso"),
                    "Mensaje: " + exception.getMessage()
            );

            // Verify
            verify(restaurantRepository, never()).findRestaurantIdsByOwnerId(any());
        }

        /**
         * Test: ID inválido (null, <= 0)
         *
         * Verificación:
         * ✅ Lanza RestaurantNotFoundException
         */
        @Test
        @DisplayName("ID <= 0 → Lanza RestaurantNotFoundException")
        void whenIdInvalid_thenThrowsRestaurantNotFoundException() {
            // Act & Assert
            assertThrows(RestaurantNotFoundException.class,
                    () -> orderService.findAllOrdersByOwnerId(0L)
            );

            // Verify
            verify(userRepository, never()).findById(any(Long.class));
        }
    }
}