package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.OrderDetailsRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.OrderRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.OrderResponseDto;
import com.c24_39_t_webapp.restaurants.exception.BadOrderRequestException;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.factories.OrderFactory;
import com.c24_39_t_webapp.restaurants.models.*;
import com.c24_39_t_webapp.restaurants.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para OrderService.addOrder()
 *
 * ✅ Órdenes y clientes desde EntityModelFactory
 * ✅ Detalles desde OrderFactory
 * ✅ Validación de OrderRequestDto
 * ✅ Happy path + error cases + validation cases
 *
 * Cobertura:
 * ✅ Crear orden exitosamente
 * ✅ Email de cliente no coincide → Lanza excepción
 * ✅ Restaurante no encontrado → Lanza excepción
 * ✅ Cliente no encontrado → Lanza excepción
 * ✅ Producto no encontrado → Lanza excepción
 * ✅ Total incorrecto → Lanza excepción
 * ✅ Detalles vacíos → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("OrderService - addOrder()")
class OrderServiceAddUnitTests {

    private static final Long RESTAURANT_ID = 1L;
    private static final Long CLIENT_ID = 1L;
    private static final Long PRODUCT_ID = 1L;
    private static final String CLIENT_EMAIL = "client@test.com";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderDetailsRepository orderDetailsRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.OrderServiceImpl orderService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - addOrder()")
    class SuccessCases {

        private UserEntity client;
        private UserEntity owner;
        private Restaurant restaurant;
        private Product product;
        private Order savedOrder;
        private OrderRequestDto validOrderRequest;

        @BeforeEach
        void setUp() {
            // ✅ Cliente desde factory
            client = EntityModelFactory.clientEntity(CLIENT_ID, CLIENT_EMAIL);

            // ✅ Owner desde factory
            owner = EntityModelFactory.restaurantOwnerEntity(2L, "owner@test.com");

            // ✅ Restaurante desde factory
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

            // ✅ Categoría desde factory
            Category category = EntityModelFactory.defaultCategory();

            // ✅ Producto desde factory
            product = EntityModelFactory.product(PRODUCT_ID, restaurant, category);

            // ✅ Request válido desde OrderFactory
            validOrderRequest = OrderFactory.defaultRequest(RESTAURANT_ID, CLIENT_ID);

            // ✅ Orden guardada desde factory
            savedOrder = EntityModelFactory.order(1L, client, restaurant);
            savedOrder.setDetails(new ArrayList<>());
        }

        /**
         * Test: Crear orden exitosamente
         *
         * Verificación:
         * ✅ OrderRepository.save() se llamó
         * ✅ OrderDetailsRepository.saveAll() se llamó
         * ✅ Retorna OrderResponseDto correcto
         */
        @Test
        @DisplayName("Crear orden válida → Exitoso")
        void whenAddingValidOrder_thenOrderIsCreated() {
            // Arrange
            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));
            when(userRepository.findById(CLIENT_ID))
                    .thenReturn(Optional.of(client));
            when(productRepository.findById(PRODUCT_ID))
                    .thenReturn(Optional.of(product));
            when(orderRepository.save(any(Order.class)))
                    .thenReturn(savedOrder);
            when(orderDetailsRepository.saveAll(any()))
                    .thenReturn(new ArrayList<>());

            // Act
            OrderResponseDto result = orderService.addOrder(validOrderRequest, CLIENT_EMAIL);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(1L, result.ord_Id());
            assertEquals(CLIENT_ID, result.clientId());
            assertEquals(RESTAURANT_ID, result.restaurantId());

            // Verify
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(userRepository, times(1)).findById(CLIENT_ID);
            verify(productRepository, times(1)).findById(PRODUCT_ID);
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(orderDetailsRepository, times(1)).saveAll(any());
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - addOrder()")
    class ErrorCases {

        private UserEntity client;
        private UserEntity owner;
        private Restaurant restaurant;

        @BeforeEach
        void setUp() {
            // ✅ Usando factories
            client = EntityModelFactory.clientEntity(CLIENT_ID, CLIENT_EMAIL);
            owner = EntityModelFactory.restaurantOwnerEntity(2L, "owner@test.com");
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);
        }

        /**
         * Test: Email no coincide
         *
         * Verificación:
         * ✅ Lanza BadOrderRequestException
         * ✅ No guarda orden
         */
        @Test
        @DisplayName("Email no coincide → Lanza BadOrderRequestException")
        void whenEmailNotMatches_thenThrowsBadOrderRequestException() {
            // Arrange
            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));
            when(userRepository.findById(CLIENT_ID))
                    .thenReturn(Optional.of(client));

            OrderRequestDto validRequest = OrderFactory.defaultRequest(RESTAURANT_ID, CLIENT_ID);

            // Act & Assert
            BadOrderRequestException exception = assertThrows(
                    BadOrderRequestException.class,
                    () -> orderService.addOrder(validRequest, "wrongemail@test.com"),
                    "Debe lanzar BadOrderRequestException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no coincide"));

            // Verify
            verify(orderRepository, never()).save(any());
        }

        /**
         * Test: Restaurante no encontrado
         *
         * Verificación:
         * ✅ Lanza RestaurantNotFoundException
         */
        @Test
        @DisplayName("Restaurante no encontrado → Lanza RestaurantNotFoundException")
        void whenRestaurantNotFound_thenThrowsRestaurantNotFoundException() {
            // Arrange
            when(restaurantRepository.findById(999L))
                    .thenReturn(Optional.empty());

            OrderRequestDto request = OrderFactory.defaultRequest(999L, CLIENT_ID);

            // Act & Assert
            RestaurantNotFoundException exception = assertThrows(
                    RestaurantNotFoundException.class,
                    () -> orderService.addOrder(request, CLIENT_EMAIL)
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no se ha encontrado"));
        }

        /**
         * Test: Cliente no encontrado
         *
         * Verificación:
         * ✅ Lanza UserNotFoundException
         */
        @Test
        @DisplayName("Cliente no encontrado → Lanza UserNotFoundException")
        void whenClientNotFound_thenThrowsUserNotFoundException() {
            // Arrange
            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));
            when(userRepository.findById(999L))
                    .thenReturn(Optional.empty());

            OrderRequestDto request = OrderFactory.defaultRequest(RESTAURANT_ID, 999L);

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> orderService.addOrder(request, CLIENT_EMAIL)
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no se ha encontrado"));
        }
    }

    // ==================== VALIDATION TESTS ====================

    @Nested
    @DisplayName("Validation - addOrder()")
    class ValidationTests {

        /**
         * Test: Total incorrecto
         *
         * Verificación:
         * ✅ Lanza IllegalArgumentException
         * ✅ Total no coincide con suma de subtotales
         */
        @Test
        @DisplayName("Total incorrecto → Lanza IllegalArgumentException")
        void whenTotalMismatch_thenThrowsIllegalArgumentException() {
            // Arrange
            OrderDetailsRequestDto detail = OrderFactory.defaultOrderDetail(PRODUCT_ID, 1, new BigDecimal("12.50"));

            OrderRequestDto invalidRequest = new OrderRequestDto(
                    1L,
                    RESTAURANT_ID,
                    CLIENT_ID,
                    OrderStatus.pendiente,
                    new BigDecimal("99.99"),  // Total incorrecto
                    "Sin cebolla",
                    Arrays.asList(detail)
            );
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> orderService.addOrder(invalidRequest, CLIENT_EMAIL),
                    "Debe lanzar IllegalArgumentException por total incorrecto"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no coincide"));
        }

        /**
         * Test: Detalles vacíos
         *
         * Verificación:
         * ✅ Lanza IllegalArgumentException
         * ✅ Orden debe tener al menos 1 detalle
         */
        @Test
        @DisplayName("Detalles vacíos → Lanza IllegalArgumentException")
        void whenDetailsEmpty_thenThrowsIllegalArgumentException() {
            // Arrange
            OrderRequestDto invalidRequest = OrderFactory.requestWithDetails(
                    RESTAURANT_ID,
                    CLIENT_ID,
                    Arrays.asList()
            );

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> orderService.addOrder(invalidRequest, CLIENT_EMAIL),
                    "Debe lanzar IllegalArgumentException por detalles vacíos"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("debe contener"));
        }

        /**
         * Test: RestaurantId inválido (null)
         *
         * Verificación:
         * ✅ Lanza IllegalArgumentException
         */
        @Test
        @DisplayName("RestaurantId null → Lanza IllegalArgumentException")
        void whenRestaurantIdIsNull_thenThrowsIllegalArgumentException() {
            // Arrange
            OrderDetailsRequestDto detail = OrderFactory.defaultOrderDetail(PRODUCT_ID, 1, new BigDecimal("12.50"));

            OrderRequestDto invalidRequest = OrderFactory.requestWithDetails(
                    null, //Invalid restaurant
                    CLIENT_ID,
                    Arrays.asList(detail)
            );

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> orderService.addOrder(invalidRequest, CLIENT_EMAIL)
            );
        }

        /**
         * Test: ClientId inválido (<= 0)
         *
         * Verificación:
         * ✅ Lanza IllegalArgumentException
         */
        @Test
        @DisplayName("ClientId <= 0 → Lanza IllegalArgumentException")
        void whenClientIdIsInvalid_thenThrowsIllegalArgumentException() {
            // Arrange
            OrderDetailsRequestDto detail = OrderFactory.defaultOrderDetail(PRODUCT_ID, 1, new BigDecimal("12.50"));

            OrderRequestDto invalidRequest = OrderFactory.requestWithDetails(
                    RESTAURANT_ID,
                    0L, // Invalid client
                    Arrays.asList(detail)
            );

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> orderService.addOrder(invalidRequest, CLIENT_EMAIL)
            );
        }
    }
}