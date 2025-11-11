package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.exception.ProductNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.repository.CategoryRepository;
import com.c24_39_t_webapp.restaurants.repository.ProductRepository;
import com.c24_39_t_webapp.restaurants.services.IProductService;
import com.c24_39_t_webapp.restaurants.services.IRestaurantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para el endpoint de borrado de productos - Endpoint DELETE /api/product/{prd_id}
 * Verifica que tras borrar un producto con un ID válido,
 * se retorna el código 204 No Content
 * También verifica los casos de error:
 * - RestaurantNotFoundException: cuando el producto no existe. Retorna 404 Not Found
 * - UnauthorizedAccessException: cuando el usuario no tiene permiso. Retorna 403 Forbidden
 * - Petición sin autenticación: retorna 401 Unauthorized
 * * Patrón AAA: Arrange, Act, Assert
 */
@WebMvcTest(
        controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@Slf4j
@DisplayName("ProductController - DELETE /api/product/{prd_id}")
public class ProductControllerDeleteTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String PRODUCT_ENDPOINT = "/api/product";
    private static final String RESTAURANT_EMAIL = "restaurante@example.com";
    private static final long PRODUCT_ID = 1L;

    /**
     * MockMvc para simular peticiones HTTP al controlador
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mock del servicio de productos
     */
    @MockitoBean
    private IProductService productService;

    /**
     * ObjectMapper para convertir objetos Java a JSON
     */
    @Autowired
    private ObjectMapper objectMapper; // Convierte objetos Java ↔ JSON


    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - DELETE /api/product/{prd_id}")
    class SuccessCases {

        /**
         * Test que verifica que al borrar un producto con un ID válido, se retorna el código 204 No Content
         * Arrange: Configura el mock del servicio para que no haga nada al borrar
         * Act & Assert: Realiza la petición DELETE y verifica el status 204
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("DELETE /api/product/{id} - Retornar 204 No Content al eliminar un producto")
        void whenDeleteProductWithValidId_thenReturnsNoContent() throws Exception {
            // Arrange: No hace nada, solo configura que no lance excepción
            doNothing().when(productService).deleteProduct(PRODUCT_ID);

            // Act & Assert
            mockMvc.perform(delete(PRODUCT_ENDPOINT + "/" + PRODUCT_ID)
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            // Verify
            verify(productService, times(1)).deleteProduct(PRODUCT_ID);
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - DELETE /api/product/{prd_id}")
    class ErrorCases {

        /**
         * Test que verifica que al intentar borrar un producto inexistente,
         * se lanza ProductNotFoundException y se retorna 404 Not Found
         * Arrange: Configura el mock del servicio para que lance ProductNotFoundException
         * Act & Assert: Realiza la petición DELETE y verifica el status 404 y el mensaje de error
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/product/{id} - Lanza 404 ProductNotFoundException si el producto no existe")
        void whenDeleteNonexistentProduct_thenReturns404() throws Exception {
            // Arrange
            doThrow(new ProductNotFoundException("Producto no encontrado con id: 999"))
                    .when(productService).deleteProduct(eq(999L));

            // Act & Assert
            mockMvc.perform(delete(PRODUCT_ENDPOINT + "/999")
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("ProductNotFoundException"));

            // Verify
            verify(productService, times(1)).deleteProduct(999L);
        }

        /**
         * Test que verifica que al intentar borrar un producto sin ser el dueño,
         * se lanza UnauthorizedAccessException y se retorna 403 Forbidden
         * Arrange: Configura el mock del servicio para que lance UnauthorizedAccessException
         * Act & Assert: Realiza la petición DELETE y verifica el status 403
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/product/{id} - Retorna 403 Forbidden si el usuario no es el dueño")
        void whenDeleteProductAsNonOwner_thenReturnsForbidden() throws Exception {
            // Arrange
            doThrow(new UnauthorizedAccessException("No tienes permiso para eliminar este producto"))
                    .when(productService).deleteProduct(eq(PRODUCT_ID));

            // Act & Assert
            mockMvc.perform(delete(PRODUCT_ENDPOINT + "/" + PRODUCT_ID)
                            .with(user("otro@example.com").roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));

            // Verify
            verify(productService, times(1)).deleteProduct(PRODUCT_ID);
        }

        /**
         * Test que verifica que al intentar borrar un producto sin autenticación, se retorna 401 Unauthorized
         * Act & Assert: Realiza la petición DELETE sin usuario y verifica el status 401
         * Verify: Verifica que el servicio NUNCA se llamó
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/product/{id} - Lanza 401 Unathorized Rechaza DELETE sin autenticación")
        void whenDeleteProductWithoutAuthentication_thenUnauthorized() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(PRODUCT_ENDPOINT + "/" + PRODUCT_ID)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NUNCA se llamó
            verify(productService, never()).deleteProduct(anyLong());
        }

        /**
         * Test que verifica que al intentar borrar un restaurante y ocurre un error interno
         * se lanza una excepción genérica InternalServerError
         * Act & Assert: Realiza la petición DELETE sin usuario y verifica el status 401
         * Verify: Verifica que el servicio NUNCA se llamó
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/product/{id} Debe retornar 500 Internal Server Error si el servicio lanza excepción")
        void whenServiceThrowsException_thenReturnsInternalError() throws Exception {
            // Arrange
            doThrow(new RuntimeException("Error en BD"))
                    .when(productService).deleteProduct(eq(PRODUCT_ID));

            // Act & Assert
            mockMvc.perform(delete(PRODUCT_ENDPOINT + "/" + PRODUCT_ID)
                            .with(user(RESTAURANT_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Error interno del servidor"));

            verify(productService, times(1)).deleteProduct(PRODUCT_ID);
        }
    }
}