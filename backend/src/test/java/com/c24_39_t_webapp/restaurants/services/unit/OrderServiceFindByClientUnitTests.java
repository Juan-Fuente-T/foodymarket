package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.OrderResponseDto;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.models.Order;
import com.c24_39_t_webapp.restaurants.models.OrderStatus;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.repository.OrderRepository;
import com.c24_39_t_webapp.restaurants.repository.RestaurantRepository;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para OrderService métodos por cliente
 *
 * ✅ findByClientId(Long cln_id)
 * ✅ findByClientIdAndCreatedAtBetween(Long cln_id, LocalDateTime start, LocalDateTime end)
 *
 * ✅ Órdenes y clientes desde EntityModelFactory
 * ✅ SecurityContext mockeado
 * ✅ Happy path + error cases + validation cases
 *
 * Cobertura:
 * ✅ Obtener órdenes por cliente
 * ✅ Cliente no encontrado → Lanza excepción
 * ✅ Usuario no autorizado → Lanza excepción
 * ✅ ID inválido → Lanza excepción
 * ✅ Obtener órdenes por cliente entre fechas
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("OrderService - Métodos por Cliente (findByClientId, findByClientIdAndCreatedAtBetween)")
class OrderServiceFindByClientUnitTests {

    private static final Long CLIENT_ID = 1L;
    private static final Long RESTAURANT_ID = 1L;
    private static final String CLIENT_EMAIL = "client@test.com";
    private static final String OTHER_EMAIL = "other@test.com";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

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

    // ==================== findByClientId() ====================

    @Nested
    @DisplayName("findByClientId() - Success Cases")
    class FindByClientIdSuccessCases {

        private UserEntity client;
        private UserEntity owner;
        private Restaurant restaurant;
        private List<Order> orders;

        @BeforeEach
        void setUp() {
            // ✅ Cliente desde factory
            client = EntityModelFactory.clientEntity(CLIENT_ID, CLIENT_EMAIL);

            // ✅ Owner y restaurante desde factory
            owner = EntityModelFactory.restaurantOwnerEntity(2L, "owner@test.com");
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

            // ✅ Órdenes desde factory
            orders = Arrays.asList(
                    EntityModelFactory.order(1L, client, restaurant, OrderStatus.pendiente, BigDecimal.valueOf(50), "Orden 1"),
                    EntityModelFactory.order(2L, client, restaurant, OrderStatus.pagado, BigDecimal.valueOf(75), "Orden 2")
            );
        }

        /**
         * Test: Obtener órdenes de cliente válido
         *
         * Verificación:
         * ✅ UserRepository.findById() se llamó
         * ✅ OrderRepository.findByClientId_Id() se llamó
         * ✅ Retorna lista con órdenes del cliente
         */
        @Test
        @DisplayName("Obtener órdenes del cliente → Retorna lista")
        void whenGettingOrdersByClientId_thenReturnsList() {
            // Arrange
            setupSecurityContextWithEmail(CLIENT_EMAIL);

            when(userRepository.findById(CLIENT_ID))
                    .thenReturn(Optional.of(client));
            when(orderRepository.findByClientId_Id(CLIENT_ID))
                    .thenReturn(orders);

            // Act
            List<OrderResponseDto> result = orderService.findByClientId(CLIENT_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(2, result.size(), "Debe retornar 2 órdenes");

            // Verify
            verify(userRepository, times(1)).findById(CLIENT_ID);
            verify(orderRepository, times(1)).findByClientId_Id(CLIENT_ID);
        }

        /**
         * Test: Lista vacía cuando cliente no tiene órdenes
         *
         * Verificación:
         * ✅ Retorna lista vacía
         */
        @Test
        @DisplayName("Cliente sin órdenes → Retorna lista vacía")
        void whenClientHasNoOrders_thenReturnsEmptyList() {
            // Arrange
            setupSecurityContextWithEmail(CLIENT_EMAIL);

            when(userRepository.findById(CLIENT_ID))
                    .thenReturn(Optional.of(client));
            when(orderRepository.findByClientId_Id(CLIENT_ID))
                    .thenReturn(new ArrayList<>());

            // Act
            List<OrderResponseDto> result = orderService.findByClientId(CLIENT_ID);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty(), "Lista debe estar vacía");
        }
    }

    @Nested
    @DisplayName("findByClientId() - Error Cases")
    class FindByClientIdErrorCases {

        /**
         * Test: Cliente no encontrado
         *
         * Verificación:
         * ✅ Lanza UserNotFoundException
         */
        @Test
        @DisplayName("Cliente no encontrado → Lanza UserNotFoundException")
        void whenClientNotFound_thenThrowsUserNotFoundException() {
            when(userRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> orderService.findByClientId(999L),
                    "Debe lanzar UserNotFoundException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("no encontrado"),
                    "Mensaje: " + exception.getMessage()
            );

            // Verify
            verify(orderRepository, never()).findByClientId_Id(any());
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

            UserEntity client = EntityModelFactory.clientEntity(CLIENT_ID, CLIENT_EMAIL);

            when(userRepository.findById(CLIENT_ID))
                    .thenReturn(Optional.of(client));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> orderService.findByClientId(CLIENT_ID),
                    "Debe lanzar UnauthorizedException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("permiso"),
                    "Mensaje: " + exception.getMessage()
            );

            // Verify
            verify(orderRepository, never()).findByClientId_Id(any());
        }

        /**
         * Test: ID inválido (null, <= 0)
         *
         * Verificación:
         * ✅ Lanza UserNotFoundException
         */
        @Test
        @DisplayName("ID <= 0 → Lanza UserNotFoundException")
        void whenIdInvalid_thenThrowsUserNotFoundException() {
            // Act & Assert
            assertThrows(UserNotFoundException.class,
                    () -> orderService.findByClientId(0L)
            );

            // Verify
            verify(userRepository, never()).findById(any(Long.class));
        }
    }

    // ==================== findByClientIdAndCreatedAtBetween() ====================

    @Nested
    @DisplayName("findByClientIdAndCreatedAtBetween() - Success Cases")
    class FindByClientIdAndCreatedAtBetweenSuccessCases {

        private UserEntity client;
        private UserEntity owner;
        private Restaurant restaurant;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<Order> orders;

        @BeforeEach
        void setUp() {
            client = EntityModelFactory.clientEntity(CLIENT_ID, CLIENT_EMAIL);
            owner = EntityModelFactory.restaurantOwnerEntity(2L, "owner@test.com");
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

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
         * ✅ OrderRepository.findByClientId_IdAndCreatedAtBetween() se llamó
         * ✅ Retorna lista con órdenes en el rango de fechas
         */
        @Test
        @DisplayName("Obtener órdenes entre fechas → Retorna lista")
        void whenGettingOrdersBetweenDates_thenReturnsList() {
            // Arrange
            setupSecurityContextWithEmail(CLIENT_EMAIL);

            when(userRepository.findById(CLIENT_ID))
                    .thenReturn(Optional.of(client));
            when(orderRepository.findByClientId_IdAndCreatedAtBetween(CLIENT_ID, startDate, endDate))
                    .thenReturn(orders);

            // Act
            List<OrderResponseDto> result = orderService.findByClientIdAndCreatedAtBetween(CLIENT_ID, startDate, endDate);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size(), "Debe retornar órdenes en el rango");

            // Verify
            verify(orderRepository, times(1)).findByClientId_IdAndCreatedAtBetween(CLIENT_ID, startDate, endDate);
        }

        /**
         * Test: Rango vacío
         *
         * Verificación:
         * ✅ Retorna lista vacía si no hay órdenes en el rango
         */
        @Test
        @DisplayName("Sin órdenes en el rango → Retorna lista vacía")
        void whenNoOrdersInRange_thenReturnsEmptyList() {
            // Arrange
            setupSecurityContextWithEmail(CLIENT_EMAIL);

            when(userRepository.findById(CLIENT_ID))
                    .thenReturn(Optional.of(client));
            when(orderRepository.findByClientId_IdAndCreatedAtBetween(CLIENT_ID, startDate, endDate))
                    .thenReturn(new ArrayList<>());

            // Act
            List<OrderResponseDto> result = orderService.findByClientIdAndCreatedAtBetween(CLIENT_ID, startDate, endDate);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty(), "Lista debe estar vacía");
        }
    }

    @Nested
    @DisplayName("findByClientIdAndCreatedAtBetween() - Error Cases")
    class FindByClientIdAndCreatedAtBetweenErrorCases {

        private LocalDateTime startDate;
        private LocalDateTime endDate;

        @BeforeEach
        void setUp() {
            startDate = LocalDateTime.now().minusDays(7);
            endDate = LocalDateTime.now();
        }

        /**
         * Test: Usuario no autorizado en búsqueda por fechas
         *
         * Verificación:
         * ✅ Lanza UnauthorizedException
         */
        @Test
        @DisplayName("Usuario no autorizado → Lanza UnauthorizedException")
        void whenUserNotAuthorized_thenThrowsUnauthorizedException() {
            // Arrange
            setupSecurityContextWithEmail(OTHER_EMAIL);

            UserEntity client = EntityModelFactory.clientEntity(CLIENT_ID, CLIENT_EMAIL);

            when(userRepository.findById(CLIENT_ID))
                    .thenReturn(Optional.of(client));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> orderService.findByClientIdAndCreatedAtBetween(CLIENT_ID, startDate, endDate),
                    "Debe lanzar UnauthorizedException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("permiso"),
                    "Mensaje: " + exception.getMessage()
            );

            // Verify
            verify(orderRepository, never()).findByClientId_IdAndCreatedAtBetween(any(), any(), any());
        }

        /**
         * Test: ID inválido en búsqueda por fechas
         *
         * Verificación:
         * ✅ Lanza UserNotFoundException
         */
        @Test
        @DisplayName("ID <= 0 en búsqueda por fechas → Lanza UserNotFoundException")
        void whenIdInvalid_thenThrowsUserNotFoundException() {
            // Act & Assert
            assertThrows(UserNotFoundException.class,
                    () -> orderService.findByClientIdAndCreatedAtBetween(0L, startDate, endDate)
            );

            // Verify
            verify(userRepository, never()).findById(any(Long.class));
        }
    }
}