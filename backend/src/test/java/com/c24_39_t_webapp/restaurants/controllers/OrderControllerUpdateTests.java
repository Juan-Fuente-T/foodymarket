package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.OrderUpdateRequestDto;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para el endpoint PATCH de OrderController
 * Verifica que al actualizar un pedido con datos válidos, se retorna 200 OK con los datos actualizados
 * También verifica los casos de error:
 * - OrderNotFoundException: cuando la orden no existe. Retorna 404 Not Found
 * - UnauthorizedAccessException: cuando el usuario no tiene permiso para editar la orden. Retorna 403 Forbidden
 * - Petición sin autenticación: retorna 401 Unauthorized
 * - Sin rol RESTAURANTE: retorna 403 Forbidden
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
@DisplayName("OrderController - PATCH /api/order/{ord_id} (Update Order)")
public class OrderControllerUpdateTests {
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

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - PATCH /api/order/{ord_id}")
    class SuccessCases {

        private OrderUpdateRequestDto validUpdateDto;
        private OrderResponseDto expectedOrderResponse;

        @BeforeEach
        void setUp() {
            // Arrange común para todos los tests de actualización
            validUpdateDto = OrderFactory.defaultUpdateRequest();
            expectedOrderResponse = OrderFactory.defaultResponse(ORDER_ID, 10L, RESTAURANT_ID);
        }

        /**
         * Test que verifica que al actualizar un pedido con datos válidos,
         * se retorna 200 OK con los datos actualizados
         * Arrange: Configura el mock del servicio para retornar el pedido actualizado
         * Act & Assert: Realiza la petición PATCH CON autenticación, rol RESTAURANTE y CSRF, verifica el status 200
         * Verify: Verifica que el servicio se llamó una sola vez con los parámetros correctos
         *
         * @throws Exception
         */
        @Test
        @DisplayName("PATCH /api/order/{ord_id} - Retorna 200 OK al actualizar un pedido")
        void whenUpdateOrderWithValidData_thenReturnsOkWithUpdatedOrderData() throws Exception {
            log.info("Iniciando test de actualización de pedido");

            // Arrange
            when(orderService.updateOrder(eq(ORDER_ID), any(OrderUpdateRequestDto.class)))
                    .thenReturn(expectedOrderResponse);

            // Act & Assert - CON autenticación, rol RESTAURANTE y CSRF
            mockMvc.perform(patch(ORDER_ENDPOINT + "/" + ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ord_Id").value(ORDER_ID))
                    .andExpect(jsonPath("$.restaurantId").value(RESTAURANT_ID));

            // Verify
            verify(orderService, times(1)).updateOrder(eq(ORDER_ID), any(OrderUpdateRequestDto.class));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - PATCH /api/order/{ord_id}")
    class ErrorCases {

        private OrderUpdateRequestDto validUpdateDto;

        @BeforeEach
        void setUp() {
            validUpdateDto = OrderFactory.updateRequestWith(OrderStatus.entregado, "Pedido entregado exitosamente");
        }

        /**
         * Test que verifica que al intentar actualizar un pedido que no existe,
         * se retorna 404 Not Found
         * Arrange: Configura el mock del servicio para lanzar OrderNotFoundException
         * Act & Assert: Realiza la petición PATCH y verifica el status 404
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/order/{ord_id} - Retorna 404 si el pedido no existe")
        void whenOrderNotFound_thenReturns404() throws Exception {
            // Arrange
            when(orderService.updateOrder(eq(999L), any(OrderUpdateRequestDto.class)))
                    .thenThrow(new OrderNotFoundException("Pedido no encontrado con ID: 999"));

            // Act & Assert - CON autenticación, rol RESTAURANTE y CSRF
            mockMvc.perform(patch(ORDER_ENDPOINT + "/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("OrderNotFoundException"));

            // Verify
            verify(orderService, times(1)).updateOrder(eq(999L), any(OrderUpdateRequestDto.class));
        }

        /**
         * Test que verifica que al intentar actualizar un pedido sin autorización,
         * se retorna 403 Forbidden
         * Arrange: Configura el mock del servicio para lanzar UnauthorizedAccessException
         * Act & Assert: Realiza la petición PATCH y verifica el status 403
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/order/{ord_id} - Retorna 403 sin permiso")
        void whenUnauthorizedAccess_thenReturnsForbidden() throws Exception {
            // Arrange
            when(orderService.updateOrder(eq(ORDER_ID), any(OrderUpdateRequestDto.class)))
                    .thenThrow(new UnauthorizedAccessException("No tienes permiso para editar pedidos de este restaurante"));

            // Act & Assert - CON autenticación, rol RESTAURANTE y CSRF
            mockMvc.perform(patch(ORDER_ENDPOINT + "/" + ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));

            // Verify
            verify(orderService, times(1)).updateOrder(eq(ORDER_ID), any(OrderUpdateRequestDto.class));
        }

        /**
         * Test que verifica que sin autenticación, se retorna 401 Unauthorized
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/order/{ord_id} - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(patch(ORDER_ENDPOINT + "/" + ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify
            verify(orderService, never()).updateOrder(any(), any());
        }
        /**
         * Test que verifica que sin rol RESTAURANTE, se retorna 403 Forbidden
         * Arrange: Configura el mock del servicio para lanzar UnauthorizedAccessException
         * Act & Assert: Realiza la petición PATCH con rol incorrecto y verifica el status 403
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/order/{ord_id} - Retorna 403 sin rol RESTAURANTE")
        void whenNoRestauranteRole_thenReturnsForbidden() throws Exception {
            // Arrange
            when(orderService.updateOrder(eq(ORDER_ID), any(OrderUpdateRequestDto.class)))
                    .thenThrow(new UnauthorizedAccessException("No tienes permiso para actualizar pedidos"));

            // Act & Assert - CON autenticación pero SIN rol RESTAURANTE
            mockMvc.perform(patch(ORDER_ENDPOINT + "/" + ORDER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(user(RESTAURANT_EMAIL).roles("CLIENTE"))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));

            // Verify
            verify(orderService, times(1)).updateOrder(eq(ORDER_ID), any(OrderUpdateRequestDto.class));
        }
    }
}