package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.exception.OrderNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.services.IOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test para el endpoint DELETE de OrderController
 * Verifica que al borrar un pedido, se retorna el código 204 No Content
 * También verifica los casos de error:
 * - OrderNotFoundException: cuando la orden no existe. Retorna 404 Not Found
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
//@EnableMethodSecurity
@DisplayName("OrderController - DELETE /api/order/{ord_id} (Delete Order)")
public class OrderControllerDeleteTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String ORDER_ENDPOINT = "/api/order";
    private static final String RESTAURANT_EMAIL = "restaurante@example.com";
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
    @DisplayName("Success Cases - DELETE /api/order/{ord_id}")
    class SuccessCases {

        /**
         * Test que verifica que al borrar un pedido válido,
         * se retorna el código 204 No Content
         * Arrange: Configura el mock del servicio para no hacer nada (void)
         * Act & Assert: Realiza la petición DELETE CON autenticación, rol RESTAURANTE y CSRF, verifica el status 204
         * Verify: Verifica que el servicio se llamó una sola vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("DELETE /api/order/{ord_id} - Retorna 204 No Content al borrar un pedido")
        void whenDeleteOrderWithValidId_thenReturnsNoContent() throws Exception {
            log.info("Iniciando test de borrado de pedido");

            // Arrange - El servicio no retorna nada (void)
            doNothing().when(orderService).deleteOrder(eq(ORDER_ID));

            // Act & Assert - CON autenticación, rol RESTAURANTE y CSRF
            mockMvc.perform(delete(ORDER_ENDPOINT + "/" + ORDER_ID)
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // Verify
            verify(orderService, times(1)).deleteOrder(eq(ORDER_ID));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - DELETE /api/order/{ord_id}")
    class ErrorCases {

        /**
         * Test que verifica que al intentar borrar un pedido que no existe,
         * se retorna el código 404 Not Found
         * Arrange: Configura el mock del servicio para lanzar OrderNotFoundException
         * Act & Assert: Realiza la petición DELETE y verifica el status 404
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/order/{ord_id} - Retorna 404 si el pedido no existe")
        void whenOrderNotFound_thenReturns404() throws Exception {
            // Arrange
            doThrow(new OrderNotFoundException("Pedido no encontrado con ID: 999"))
                    .when(orderService).deleteOrder(eq(999L));

            // Act & Assert - CON autenticación, rol RESTAURANTE y CSRF
            mockMvc.perform(delete(ORDER_ENDPOINT + "/999")
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("OrderNotFoundException"));

            // Verify
            verify(orderService, times(1)).deleteOrder(eq(999L));
        }

        /**
         * Test que verifica que sin autenticación, se retorna 401 Unauthorized
         * Arrange: No se configura autenticación
         * Act & Assert: Realiza la petición DELETE sin auth y verifica el status 401
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/order/{ord_id} - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(delete(ORDER_ENDPOINT + "/" + ORDER_ID)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NO fue llamado
            verify(orderService, never()).deleteOrder(any());
        }

        /**
         * Test que verifica que sin rol RESTAURANTE, se retorna 403 Forbidden
         * Arrange: Configura el mock del servicio para lanzar UnauthorizedAccessException
         * Act & Assert: Realiza la petición DELETE con rol incorrecto y verifica el status 403
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/order/{ord_id} - Retorna 403 sin rol RESTAURANTE")
        void whenNoRestauranteRole_thenReturnsForbidden() throws Exception {
            // Arrange - Hacer que el servicio lance la excepción
            doThrow(new UnauthorizedAccessException("No tienes permiso para eliminar este pedido"))
                    .when(orderService).deleteOrder(eq(ORDER_ID));

            // Act & Assert - CON autenticación pero SIN rol RESTAURANTE
            mockMvc.perform(delete(ORDER_ENDPOINT + "/" + ORDER_ID)
                            .with(user(RESTAURANT_EMAIL).roles("CLIENTE"))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));

            // Verify
            verify(orderService, times(1)).deleteOrder(eq(ORDER_ID));
        }
    }
    }
