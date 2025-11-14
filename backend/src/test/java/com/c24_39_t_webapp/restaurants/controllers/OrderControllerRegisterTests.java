package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.OrderRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.OrderResponseDto;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.OrderFactory;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para el endpoint POST de OrderController
 * Verifica que al crear un pedido con datos válidos, se retorna el código 200 OK con los datos del pedido creado
 * También verifica los casos de error:
 * - RestaurantNotFoundException: cuando el restaurante no existe. Retorna 404 Not Found
 * - UserNotFoundException: cuando el usuario/cliente no existe. Retorna 404 Not Found
 * - Datos inválidos (restaurantId ≤ 0, clientId ≤ 0, etc.): retorna 400 Bad Request
 * - Petición sin autenticación: retorna 401 Unauthorized
 * - Sin rol CLIENTE: retorna 403 Forbidden
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
@DisplayName("OrderController - POST /api/order (Create Order)")
public class OrderControllerRegisterTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String ORDER_ENDPOINT = "/api/order";
    private static final String CLIENT_EMAIL = "cliente@example.com";
    private static final long CLIENT_ID = 10L;
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

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - POST /api/order")
    class SuccessCases {

        private OrderRequestDto validOrderDto;
        private OrderResponseDto expectedOrderResponse;

        @BeforeEach
        void setUp() {
            // Arrange común para todos los tests de creación
            validOrderDto = OrderFactory.defaultRequest(RESTAURANT_ID, CLIENT_ID);
            expectedOrderResponse = OrderFactory.responseFromRequest(validOrderDto, ORDER_ID);
        }

        /**
         * Test que verifica que al crear un pedido con datos válidos,
         * se retorna el código 200 OK con los datos del pedido creado
         * Arrange: Configura el mock del servicio para retornar el pedido creado
         * Act & Assert: Realiza la petición POST CON autenticación, rol CLIENTE y CSRF, verifica el status 200 y los datos
         * Verify: Verifica que el servicio se llamó una sola vez con los parámetros correctos
         *
         * @throws Exception
         */
        @Test
        @DisplayName("POST /api/order - Retorna 200 OK al crear un pedido válido")
        void whenCreateOrderWithValidData_thenReturnsOkWithOrderData() throws Exception {
            log.info("Iniciando test de creación de pedido");

            // Arrange
            when(orderService.addOrder(any(OrderRequestDto.class), eq(CLIENT_EMAIL)))
                    .thenReturn(expectedOrderResponse);

            // Act & Assert - CON autenticación, rol CLIENTE y CSRF
            mockMvc.perform(post(ORDER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validOrderDto))
                            .param("email", CLIENT_EMAIL)
                            .with(user(CLIENT_EMAIL).roles("CLIENTE"))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ord_Id").value(ORDER_ID))
                    .andExpect(jsonPath("$.clientId").value(CLIENT_ID))
                    .andExpect(jsonPath("$.restaurantId").value(RESTAURANT_ID));

            // Verify
            verify(orderService, times(1)).addOrder(any(OrderRequestDto.class), eq(CLIENT_EMAIL));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - POST /api/order")
    class ErrorCases {

        private OrderRequestDto validOrderDto;

        @BeforeEach
        void setUp() {
            validOrderDto = OrderFactory.defaultRequest(RESTAURANT_ID, CLIENT_ID);
        }

        /**
         * Test que verifica que al intentar crear un pedido para un restaurante inexistente,
         * se retorna el código 404 Not Found
         * Arrange: Configura el mock del servicio para lanzar RestaurantNotFoundException
         * Act & Assert: Realiza la petición POST y verifica el status 404
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail POST /api/order - Retorna 404 si el restaurante no existe")
        void whenRestaurantNotFound_thenReturns404() throws Exception {
            // Arrange
            when(orderService.addOrder(any(OrderRequestDto.class), eq(CLIENT_EMAIL)))
                    .thenThrow(new RestaurantNotFoundException("No se ha encontrado el restaurante"));

            // Act & Assert - CON autenticación, rol CLIENTE y CSRF
            mockMvc.perform(post(ORDER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validOrderDto))
                            .param("email", CLIENT_EMAIL)
                            .with(user(CLIENT_EMAIL).roles("CLIENTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("RestaurantNotFoundException"));

            // Verify
            verify(orderService, times(1)).addOrder(any(OrderRequestDto.class), eq(CLIENT_EMAIL));
        }

        /**
         * Test que verifica que al intentar crear un pedido para un usuario/cliente inexistente,
         * se retorna el código 404 Not Found
         * Arrange: Configura el mock del servicio para lanzar UserNotFoundException
         * Act & Assert: Realiza la petición POST y verifica el status 404
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail POST /api/order - Retorna 404 si el cliente no existe")
        void whenClientNotFound_thenReturns404() throws Exception {
            // Arrange
            when(orderService.addOrder(any(OrderRequestDto.class), eq(CLIENT_EMAIL)))
                    .thenThrow(new UserNotFoundException("No se ha encontrado el usuario"));

            // Act & Assert - CON autenticación, rol CLIENTE y CSRF
            mockMvc.perform(post(ORDER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validOrderDto))
                            .param("email", CLIENT_EMAIL)
                            .with(user(CLIENT_EMAIL).roles("CLIENTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("UserNotFoundException"));

            // Verify
            verify(orderService, times(1)).addOrder(any(OrderRequestDto.class), eq(CLIENT_EMAIL));
        }

        /**
         * Test que verifica que al intentar crear un pedido sin autenticación,
         * se retorna el código 401 Unauthorized
         * Arrange: No se configura autenticación
         * Act & Assert: Realiza la petición POST sin auth y verifica el status 401
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail POST /api/order - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(post(ORDER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validOrderDto))
                            .param("email", CLIENT_EMAIL)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NO fue llamado
            verify(orderService, never()).addOrder(any(), any());
        }
        /**
         * Test que verifica que al intentar crear un pedido sin el rol CLIENTE,
         * se retorna el código 403 Forbidden
         * Arrange: Configura el mock del servicio para lanzar UnauthorizedAccessException
         * Act & Assert: Realiza la petición POST y verifica el status 403
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail POST /api/order - Retorna 403 sin rol CLIENTE")
        void whenNoClientRole_thenReturnsForbidden() throws Exception {
            // Arrange
            doThrow(new UnauthorizedAccessException("No tienes permiso para crear pedidos"))
                    .when(orderService).addOrder(any(OrderRequestDto.class), eq(CLIENT_EMAIL));

            // Act & Assert - CON autenticación pero SIN rol CLIENTE
            mockMvc.perform(post(ORDER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validOrderDto))
                            .param("email", CLIENT_EMAIL)
                            .with(user(CLIENT_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));

            // Verify
            verify(orderService, times(1)).addOrder(any(OrderRequestDto.class), eq(CLIENT_EMAIL));
        }
    }
}