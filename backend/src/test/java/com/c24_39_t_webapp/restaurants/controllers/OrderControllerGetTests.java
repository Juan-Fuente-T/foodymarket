package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.response.OrderResponseDto;
import com.c24_39_t_webapp.restaurants.exception.OrderNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.OrderFactory;
import com.c24_39_t_webapp.restaurants.models.OrderStatus;
import com.c24_39_t_webapp.restaurants.services.IOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para los endpoints GET de OrderController (rol RESTAURANTE)
 * Verifica que al obtener órdenes con criterios válidos, se retorna 200 OK con la lista de órdenes
 * También verifica los casos de error:
 * - OrderNotFoundException: cuando la orden no existe. Retorna 404 Not Found
 * - Petición sin autenticación: retorna 401 Unauthorized
 *
 * Patrón AAA: Arrange, Act, Assert
 */
@WebMvcTest(
        controllers = OrderController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@Slf4j
@DisplayName("OrderController - GET /api/order/* (Get Orders)")
public class OrderControllerGetTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String ORDER_ENDPOINT = "/api/order";
    private static final String RESTAURANT_EMAIL = "restaurante@example.com";
    private static final long RESTAURANT_ID = 1L;
    private static final long ORDER_ID = 1L;

    /**
     * MockMvc para simular peticiones HTTP al controlador
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mock del servicio de órdenes
     */
    @MockitoBean
    private IOrderService orderService;

    /**
     * ObjectMapper para convertir objetos Java a JSON
     */
    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET ALL ORDERS ====================

    @Nested
    @DisplayName("GET /api/order (Get All Orders)")
    class GetAllOrdersTests {

        private List<OrderResponseDto> mockOrdersList;

        @BeforeEach
        void setUp() {
            mockOrdersList = OrderFactory.responseListDefault();
        }

        /**
         * Test que verifica que al obtener todos los pedidos de un restaurante,
         * se retorna 200 OK con la lista de pedidos
         * Arrange: Configura el mock del servicio para retornar la lista de órdenes
         * Act & Assert: Realiza la petición GET CON autenticación y rol RESTAURANTE, verifica el status 200 y la lista
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/order - Retorna 200 OK con lista de pedidos del restaurante")
        void whenGetAllOrders_thenReturnsOkWithOrdersList() throws Exception {
            log.info("Iniciando test de obtención de todos los pedidos");

            // Arrange
            when(orderService.findAllOrders(eq(RESTAURANT_ID)))
                    .thenReturn(mockOrdersList);

            // Act & Assert - CON autenticación, rol RESTAURANTE
            mockMvc.perform(get(ORDER_ENDPOINT)
                            .param("restaurantId", String.valueOf(RESTAURANT_ID))
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(mockOrdersList.size())));

            // Verify
            verify(orderService, times(1)).findAllOrders(eq(RESTAURANT_ID));
        }

        /**
         * Test que verifica que sin autenticación, se retorna 401 Unauthorized
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/order - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(get(ORDER_ENDPOINT)
                            .param("restaurantId", String.valueOf(RESTAURANT_ID)))
                    .andExpect(status().isUnauthorized());

            // Verify
            verify(orderService, never()).findAllOrders(any());
        }
        /**
         * Test que verifica que al intentar obtener pedidos sin el rol RESTAURANTE,
         * se lanza UnauthorizedAccessException y se retorna 403 Forbidden
         * Arrange: Configura el mock del servicio para que lance UnauthorizedAccessException
         * Act & Assert: Realiza la petición GET y verifica el status 403
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/order - Retorna 403 sin rol RESTAURANTE")
        void whenNoRestauranteRole_thenReturnsForbidden() throws Exception {
            // Arrange
            when(orderService.findAllOrders(eq(RESTAURANT_ID)))
                    .thenThrow(new UnauthorizedAccessException("No tienes permiso para ver pedidos"));

            // Act & Assert - CON autenticación pero SIN rol RESTAURANTE
            mockMvc.perform(get(ORDER_ENDPOINT)
                            .param("restaurantId", String.valueOf(RESTAURANT_ID))
                            .with(user(RESTAURANT_EMAIL).roles("CLIENTE")))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));

            // Verify
            verify(orderService, times(1)).findAllOrders(eq(RESTAURANT_ID));
        }
    }

    // ==================== GET ORDER BY ID ====================

    @Nested
    @DisplayName("GET /api/order/{ord_id} (Get Order By ID)")
    class GetOrderByIdTests {

        private OrderResponseDto expectedOrder;

        @BeforeEach
        void setUp() {
            expectedOrder = OrderFactory.defaultResponse(ORDER_ID, 10L, RESTAURANT_ID);
        }

        /**
         * Test que verifica que al obtener una orden por ID válido,
         * se retorna 200 OK con los datos de la orden
         * Arrange: Configura el mock del servicio para retornar la orden
         * Act & Assert: Realiza la petición GET CON autenticación y verifica el status 200
         * Verify: Verifica que el servicio se llamó una sola vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/order/{ord_id} - Retorna 200 OK con datos del pedido")
        void whenGetOrderById_thenReturnsOkWithOrderData() throws Exception {
            log.info("Iniciando test de obtención de pedido por ID");

            // Arrange
            when(orderService.findOrderById(eq(ORDER_ID)))
                    .thenReturn(expectedOrder);

            // Act & Assert - CON autenticación, rol RESTAURANTE
            mockMvc.perform(get(ORDER_ENDPOINT + "/" + ORDER_ID)
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ord_Id").value(ORDER_ID))
                    .andExpect(jsonPath("$.restaurantId").value(RESTAURANT_ID));

            // Verify
            verify(orderService, times(1)).findOrderById(eq(ORDER_ID));
        }

        /**
         * Test que verifica que al obtener una orden que no existe,
         * se retorna 404 Not Found
         * Arrange: Configura el mock del servicio para lanzar OrderNotFoundException
         * Act & Assert: Realiza la petición GET y verifica el status 404
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/order/{ord_id} - Retorna 404 si el pedido no existe")
        void whenOrderNotFound_thenReturns404() throws Exception {
            // Arrange
            when(orderService.findOrderById(eq(999L)))
                    .thenThrow(new OrderNotFoundException("Pedido no encontrado con ID: 999"));

            // Act & Assert - CON autenticación, rol RESTAURANTE
            mockMvc.perform(get(ORDER_ENDPOINT + "/999")
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("OrderNotFoundException"));

            // Verify
            verify(orderService, times(1)).findOrderById(eq(999L));
        }
    }
// ==================== GET ORDERS BY DATE ====================

    @Nested
    @DisplayName("GET /api/order/byDate (Get Orders By Date Range)")
    class GetOrdersByDateTests {

        private List<OrderResponseDto> mockOrdersList;
        private LocalDateTime startDate;
        private LocalDateTime endDate;

        @BeforeEach
        void setUp() {
            mockOrdersList = OrderFactory.responseListDefault();
            startDate = LocalDateTime.now().minusDays(7);
            endDate = LocalDateTime.now();
        }

        /**
         * Test que verifica que al obtener pedidos por rango de fechas,
         * se retorna 200 OK con la lista de pedidos en ese rango
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/order/byDate - Retorna 200 OK con pedidos en rango de fechas")
        void whenGetOrdersByDate_thenReturnsOkWithOrdersList() throws Exception {
            // Arrange
            when(orderService.findByCreatedAtBetween(eq(RESTAURANT_ID), eq(startDate), eq(endDate)))
                    .thenReturn(mockOrdersList);

            // Act & Assert - CON autenticación, rol RESTAURANTE
            mockMvc.perform(get(ORDER_ENDPOINT + "/byDate")
                            .param("restaurantId", String.valueOf(RESTAURANT_ID))
                            .param("start", startDate.toString())
                            .param("end", endDate.toString())
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(mockOrdersList.size())));

            // Verify
            verify(orderService, times(1)).findByCreatedAtBetween(eq(RESTAURANT_ID), eq(startDate), eq(endDate));
        }
    }

    // ==================== GET ORDERS BY CLIENT ID ====================

    @Nested
    @DisplayName("GET /api/order/byClientId/{cln_id} (Get Orders By Client ID)")
    class GetOrdersByClientIdTests {

        private List<OrderResponseDto> mockOrdersList;
        private static final long CLIENT_ID = 10L;

        @BeforeEach
        void setUp() {
            mockOrdersList = OrderFactory.responseListDefault();
        }

        /**
         * Test que verifica que al obtener pedidos de un cliente,
         * se retorna 200 OK con la lista de pedidos del cliente
         * Requiere rol CLIENTE
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/order/byClientId/{cln_id} - Retorna 200 OK con pedidos del cliente")
        void whenGetOrdersByClientId_thenReturnsOkWithOrdersList() throws Exception {
            // Arrange
            when(orderService.findByClientId(eq(CLIENT_ID)))
                    .thenReturn(mockOrdersList);

            // Act & Assert - CON autenticación, rol CLIENTE
            mockMvc.perform(get(ORDER_ENDPOINT + "/byClientId/" + CLIENT_ID)
                            .with(user("cliente@example.com").roles("CLIENTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(mockOrdersList.size())));

            // Verify
            verify(orderService, times(1)).findByClientId(eq(CLIENT_ID));
        }

    }

    // ==================== GET ORDERS BY OWNER ID ====================

    @Nested
    @DisplayName("GET /api/order/byOwnerId/{ownerId} (Get Orders By Owner ID)")
    class GetOrdersByOwnerIdTests {

        private List<OrderResponseDto> mockOrdersList;
        private static final long OWNER_ID = 1L;

        @BeforeEach
        void setUp() {
            mockOrdersList = OrderFactory.responseListDefault();
        }

        /**
         * Test que verifica que al obtener pedidos de un propietario de restaurante,
         * se retorna 200 OK con la lista de pedidos
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/order/byOwnerId/{ownerId} - Retorna 200 OK con pedidos del dueño")
        void whenGetOrdersByOwnerId_thenReturnsOkWithOrdersList() throws Exception {
            // Arrange
            when(orderService.findAllOrdersByOwnerId(eq(OWNER_ID)))
                    .thenReturn(mockOrdersList);

            // Act & Assert - CON autenticación, rol RESTAURANTE
            mockMvc.perform(get(ORDER_ENDPOINT + "/byOwnerId/" + OWNER_ID)
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(mockOrdersList.size())));

            // Verify
            verify(orderService, times(1)).findAllOrdersByOwnerId(eq(OWNER_ID));
        }
    }

    // ==================== GET ORDERS BY OWNER ID PAGED ====================

    @Nested
    @DisplayName("GET /api/order/byOwnerId/{ownerId}/paged (Get Orders By Owner ID Paged)")
    class GetOrdersByOwnerIdPagedTests {

        private Page<OrderResponseDto> mockOrdersPage;
        private static final long OWNER_ID = 1L;

        @BeforeEach
        void setUp() {
            List<OrderResponseDto> ordersList = OrderFactory.responseListDefault();
            mockOrdersPage = new PageImpl<>(ordersList, PageRequest.of(0, 20), ordersList.size());
        }

        /**
         * Test que verifica que al obtener pedidos de un propietario con paginación,
         * se retorna 200 OK con la página de pedidos
         * Requiere validación especial: #ownerId == authentication.principal.id or hasRole('ADMIN')
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/order/byOwnerId/{ownerId}/paged - Retorna 200 OK con página de pedidos")
        void whenGetOrdersByOwnerIdPaged_thenReturnsOkWithPagedOrders() throws Exception {
            // Arrange
            when(orderService.findOrdersByOwnerIdPaged(eq(OWNER_ID), any()))
                    .thenReturn(mockOrdersPage);

            // Act & Assert - CON autenticación y rol RESTAURANTE
            mockMvc.perform(get(ORDER_ENDPOINT + "/byOwnerId/" + OWNER_ID + "/paged")
                            .param("page", "0")
                            .param("size", "20")
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(3)));

            // Verify
            verify(orderService, times(1)).findOrdersByOwnerIdPaged(eq(OWNER_ID), any());
        }
    }

    // ==================== GET ORDERS BY CLIENT DATE ====================

    @Nested
    @DisplayName("GET /api/order/byClientDate (Get Orders By Client ID and Date Range)")
    class GetOrdersByClientDateTests {

        private List<OrderResponseDto> mockOrdersList;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private static final long CLIENT_ID = 10L;

        @BeforeEach
        void setUp() {
            mockOrdersList = OrderFactory.responseListDefault();
            startDate = LocalDateTime.now().minusDays(7);
            endDate = LocalDateTime.now();
        }

        /**
         * Test que verifica que al obtener pedidos de un cliente en un rango de fechas,
         * se retorna 200 OK con la lista de pedidos
         * Requiere rol CLIENTE
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/order/byClientDate - Retorna 200 OK con pedidos del cliente en rango")
        void whenGetOrdersByClientDate_thenReturnsOkWithOrdersList() throws Exception {
            // Arrange
            when(orderService.findByClientIdAndCreatedAtBetween(eq(CLIENT_ID), eq(startDate), eq(endDate)))
                    .thenReturn(mockOrdersList);

            // Act & Assert - CON autenticación, rol CLIENTE
            mockMvc.perform(get(ORDER_ENDPOINT + "/byClientDate")
                            .param("clientId", String.valueOf(CLIENT_ID))
                            .param("start", startDate.toString())
                            .param("end", endDate.toString())
                            .with(user("cliente@example.com").roles("CLIENTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(mockOrdersList.size())));

            // Verify
            verify(orderService, times(1)).findByClientIdAndCreatedAtBetween(eq(CLIENT_ID), eq(startDate), eq(endDate));
        }
    }

    // ==================== GET ORDERS BY RESTAURANT AND STATE ====================

    @Nested
    @DisplayName("GET /api/order/byRestaurantAndState (Get Orders By Restaurant and Status)")
    class GetOrdersByRestaurantAndStateTests {

        private List<OrderResponseDto> mockOrdersList;

        @BeforeEach
        void setUp() {
            mockOrdersList = OrderFactory.responseListDefault();
        }

        /**
         * Test que verifica que al obtener pedidos por estado y restaurante,
         * se retorna 200 OK con la lista de pedidos en ese estado
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/order/byRestaurantAndState - Retorna 200 OK con pedidos del estado")
        void whenGetOrdersByRestaurantAndState_thenReturnsOkWithOrdersList() throws Exception {
            // Arrange
            when(orderService.findByStatusAndRestaurantId(eq(OrderStatus.pendiente), eq(RESTAURANT_ID)))
                    .thenReturn(mockOrdersList);

            // Act & Assert - CON autenticación, rol RESTAURANTE
            mockMvc.perform(get(ORDER_ENDPOINT + "/byRestaurantAndState")
                            .param("status", "pendiente")
                            .param("restaurantId", String.valueOf(RESTAURANT_ID))
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(mockOrdersList.size())));

            // Verify
            verify(orderService, times(1)).findByStatusAndRestaurantId(eq(OrderStatus.pendiente), eq(RESTAURANT_ID));
        }
    }
}