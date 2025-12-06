package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.OrderResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para OrderService.findOrdersByOwnerIdPaged()
 *
 * ✅ Órdenes, restaurantes y users desde EntityModelFactory
 * ✅ SecurityContext mockeado
 * ✅ Paginación con Spring Data
 * ✅ Happy path + error cases + edge cases
 *
 * Cobertura:
 * ✅ Obtener órdenes del owner con paginación
 * ✅ Primera página
 * ✅ Segunda página
 * ✅ Página vacía
 * ✅ Owner sin restaurantes
 * ✅ Usuario no autorizado → Lanza excepción
 * ✅ Owner no encontrado → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("OrderService - findOrdersByOwnerIdPaged()")
class OrderServiceFindOrdersPagedUnitTests {

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

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - findOrdersByOwnerIdPaged()")
    class SuccessCases {

        private UserEntity owner;
        private UserEntity client;
        private Restaurant restaurant;
        private List<Order> orders;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
            owner = EntityModelFactory.restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);
            client = EntityModelFactory.clientEntity(1L, "client@test.com");
            restaurant = EntityModelFactory.restaurant(1L, owner);

            // Crear 5 órdenes para pruebas de paginación
            orders = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                orders.add(EntityModelFactory.order((long) i, client, restaurant,
                        OrderStatus.pendiente, BigDecimal.valueOf(50 * i), "Orden " + i));
            }

            pageable = PageRequest.of(0, 2);  // Página 0, 2 elementos por página
        }

        /**
         * Test: Obtener primera página
         *
         * Verificación:
         * ✅ OrderRepository.findByRestaurantId_IdIn() se llamó con paginación
         * ✅ Retorna Page con elementos correctos
         * ✅ getTotalElements() es correcto
         */
        @Test
        @DisplayName("Obtener primera página → Retorna Page correcta")
        void whenGettingFirstPage_thenReturnsFirstPageOfOrders() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            List<Order> pageContent = orders.subList(0, 2);  // Primeros 2 elementos
            Page<Order> orderPage = new PageImpl<>(pageContent, pageable, 5);  // 5 elementos totales

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));
            when(restaurantRepository.findRestaurantIdsByOwnerId(OWNER_ID))
                    .thenReturn(Arrays.asList(1L));
            when(orderRepository.findByRestaurantId_IdIn(Arrays.asList(1L), pageable))
                    .thenReturn(orderPage);

            // Act
            Page<OrderResponseDto> result = orderService.findOrdersByOwnerIdPaged(OWNER_ID, pageable);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(2, result.getContent().size(), "Debe retornar 2 elementos en la página");
            assertEquals(5, result.getTotalElements(), "Total de elementos debe ser 5");
            assertTrue(result.hasNext(), "Debe haber siguiente página");

            // Verify
            verify(userRepository, times(1)).findById(OWNER_ID);
            verify(restaurantRepository, times(1)).findRestaurantIdsByOwnerId(OWNER_ID);
            verify(orderRepository, times(1)).findByRestaurantId_IdIn(Arrays.asList(1L), pageable);
        }

        /**
         * Test: Obtener segunda página
         *
         * Verificación:
         * ✅ Retorna elementos correctos de la segunda página
         */
        @Test
        @DisplayName("Obtener segunda página → Retorna Page correcta")
        void whenGettingSecondPage_thenReturnsSecondPageOfOrders() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            Pageable pageablePage2 = PageRequest.of(1, 2);  // Página 1 (segunda), 2 elementos
            List<Order> pageContent = orders.subList(2, 4);  // Elementos 3-4
            Page<Order> orderPage = new PageImpl<>(pageContent, pageablePage2, 5);

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));
            when(restaurantRepository.findRestaurantIdsByOwnerId(OWNER_ID))
                    .thenReturn(Arrays.asList(1L));
            when(orderRepository.findByRestaurantId_IdIn(Arrays.asList(1L), pageablePage2))
                    .thenReturn(orderPage);

            // Act
            Page<OrderResponseDto> result = orderService.findOrdersByOwnerIdPaged(OWNER_ID, pageablePage2);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getContent().size(), "Debe retornar 2 elementos");
            assertEquals(5, result.getTotalElements(), "Total sigue siendo 5");
            assertTrue(result.hasNext(), "Debe haber siguiente página (página 3)");

            // Verify
            verify(orderRepository, times(1)).findByRestaurantId_IdIn(Arrays.asList(1L), pageablePage2);
        }

        /**
         * Test: Última página incompleta
         *
         * Verificación:
         * ✅ Retorna menos elementos que el tamaño de página
         * ✅ No hay siguiente página
         */
        @Test
        @DisplayName("Última página incompleta → Retorna elementos restantes")
        void whenGettingLastIncompletePage_thenReturnsRemainingElements() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            Pageable pageablePage3 = PageRequest.of(2, 2);  // Página 2 (tercera)
            List<Order> pageContent = orders.subList(4, 5);  // Solo el último elemento
            Page<Order> orderPage = new PageImpl<>(pageContent, pageablePage3, 5);

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));
            when(restaurantRepository.findRestaurantIdsByOwnerId(OWNER_ID))
                    .thenReturn(Arrays.asList(1L));
            when(orderRepository.findByRestaurantId_IdIn(Arrays.asList(1L), pageablePage3))
                    .thenReturn(orderPage);

            // Act
            Page<OrderResponseDto> result = orderService.findOrdersByOwnerIdPaged(OWNER_ID, pageablePage3);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getContent().size(), "Debe retornar 1 elemento");
            assertFalse(result.hasNext(), "No debe haber siguiente página");

            // Verify
            verify(orderRepository, times(1)).findByRestaurantId_IdIn(Arrays.asList(1L), pageablePage3);
        }

        /**
         * Test: Página válida pero vacía
         *
         * Verificación:
         * ✅ Retorna Page vacía
         */
        @Test
        @DisplayName("Página válida pero vacía → Retorna Page.empty()")
        void whenPageOutOfRange_thenReturnsEmptyPage() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            Pageable pageableOutOfRange = PageRequest.of(5, 2);  // Página muy alta
            Page<Order> emptyPage = Page.empty(pageableOutOfRange);

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));
            when(restaurantRepository.findRestaurantIdsByOwnerId(OWNER_ID))
                    .thenReturn(Arrays.asList(1L));
            when(orderRepository.findByRestaurantId_IdIn(Arrays.asList(1L), pageableOutOfRange))
                    .thenReturn(emptyPage);

            // Act
            Page<OrderResponseDto> result = orderService.findOrdersByOwnerIdPaged(OWNER_ID, pageableOutOfRange);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty(), "Página debe estar vacía");
            assertEquals(0, result.getContent().size());
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases - findOrdersByOwnerIdPaged()")
    class EdgeCases {

        private UserEntity owner;

        @BeforeEach
        void setUp() {
            owner = EntityModelFactory.restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);
        }

        /**
         * Test: Owner sin restaurantes
         *
         * Verificación:
         * ✅ Retorna Page.empty()
         * ✅ No llama a OrderRepository
         */
        @Test
        @DisplayName("Owner sin restaurantes → Retorna Page.empty()")
        void whenOwnerHasNoRestaurants_thenReturnsEmptyPage() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            Pageable pageable = PageRequest.of(0, 2);

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));
            when(restaurantRepository.findRestaurantIdsByOwnerId(OWNER_ID))
                    .thenReturn(new ArrayList<>());

            // Act
            Page<OrderResponseDto> result = orderService.findOrdersByOwnerIdPaged(OWNER_ID, pageable);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty(), "Debe retornar Page.empty()");

            // Verify
            verify(orderRepository, never()).findByRestaurantId_IdIn(any(), any());
        }

        /**
         * Test: Owner con múltiples restaurantes
         *
         * Verificación:
         * ✅ Retorna órdenes de TODOS los restaurantes del owner
         */
        @Test
        @DisplayName("Owner con múltiples restaurantes → Retorna órdenes de todos")
        void whenOwnerHasMultipleRestaurants_thenReturnsOrdersFromAll() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            UserEntity client = EntityModelFactory.clientEntity(1L, "client@test.com");
            Restaurant restaurant1 = EntityModelFactory.restaurant(1L, owner);
            Restaurant restaurant2 = EntityModelFactory.restaurant(2L, owner);

            Order order1 = EntityModelFactory.order(1L, client, restaurant1, OrderStatus.pendiente, BigDecimal.valueOf(50), "Orden 1");
            Order order2 = EntityModelFactory.order(2L, client, restaurant2, OrderStatus.pagado, BigDecimal.valueOf(75), "Orden 2");

            Pageable pageable = PageRequest.of(0, 10);
            Page<Order> orderPage = new PageImpl<>(Arrays.asList(order1, order2), pageable, 2);

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));
            when(restaurantRepository.findRestaurantIdsByOwnerId(OWNER_ID))
                    .thenReturn(Arrays.asList(1L, 2L));
            when(orderRepository.findByRestaurantId_IdIn(Arrays.asList(1L, 2L), pageable))
                    .thenReturn(orderPage);

            // Act
            Page<OrderResponseDto> result = orderService.findOrdersByOwnerIdPaged(OWNER_ID, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getContent().size(), "Debe retornar órdenes de ambos restaurantes");

            // Verify
            verify(restaurantRepository, times(1)).findRestaurantIdsByOwnerId(OWNER_ID);
            verify(orderRepository, times(1)).findByRestaurantId_IdIn(Arrays.asList(1L, 2L), pageable);
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - findOrdersByOwnerIdPaged()")
    class ErrorCases {

        /**
         * Test: Owner no encontrado
         *
         * Verificación:
         * ✅ Lanza UserNotFoundException
         */
        @Test
        @DisplayName("Owner no encontrado → Lanza UserNotFoundException")
        void whenOwnerNotFound_thenThrowsUserNotFoundException() {
            Pageable pageable = PageRequest.of(0, 2);

            when(userRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> orderService.findOrdersByOwnerIdPaged(999L, pageable)
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
            Pageable pageable = PageRequest.of(0, 2);

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> orderService.findOrdersByOwnerIdPaged(OWNER_ID, pageable)
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("permiso"),
                    "Mensaje: " + exception.getMessage()
            );

            // Verify
            verify(restaurantRepository, never()).findRestaurantIdsByOwnerId(any());
        }
    }
}
