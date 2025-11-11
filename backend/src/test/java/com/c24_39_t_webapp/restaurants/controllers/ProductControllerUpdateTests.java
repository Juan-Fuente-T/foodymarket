package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.ProductUpdateDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.ProductNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.services.IProductService;
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

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para el endpoint de actualizar productos - Endpoint PATCH (Update) /api/product/{prd_id}
 * Arquitectura aplicada:
 * - @WebMvcTest: Solo carga ProductController (slice testing)
 * - @MockitoBean: Mockea IProductService
 * - @Nested: Agrupa Success Cases y Error Cases
 * - AAA Pattern: Arrange, Act, Assert en cada test
 * - Patrón Given-When-Then implícito en los tests
 * - Uso de constantes para evitar "magic strings"
 * Cobertura:
 * ✅ PATCH /api/product/{prd_id} con datos válidos → 200 OK
 * ✅ PATCH /api/product/{prd_id} actualización parcial → 200 OK
 * ✅ PATCH /api/product/{prd_id} producto no existe → 404 Not Found
 * ✅ PATCH /api/product/{prd_id} categoría no existe → 404 Not Found
 * ✅ PATCH /api/product/{prd_id} sin permiso (no es dueño) → 403 Forbidden
 * ✅ PATCH /api/product/{prd_id} sin autenticación → 401 Unauthorized
 */
@WebMvcTest(
        controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@Slf4j
@DisplayName("ProductController - PATCH /api/product/{prd_id} (Update)")
public class ProductControllerUpdateTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String PRODUCT_ENDPOINT = "/api/product";
    private static final String RESTAURANTE_EMAIL = "restaurante@example.com";
    private static final long PRODUCT_ID = 1L;
    private static final long RESTAURANT_ID = 1L;
    private static final long CATEGORY_ID = 1L;

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
    @DisplayName("Success Cases - PATCH /api/product/{prd_id}")
    class SuccessCases {

        private ProductUpdateDto validUpdateDto;
        private ProductResponseDto expectedUpdatedProduct;

        @BeforeEach
        void setUp() {
            validUpdateDto = new ProductUpdateDto(
                    RESTAURANT_ID,
                    CATEGORY_ID,
                    "Pizza Margherita Mejorada",
                    "Auténtica pizza italiana con ingredientes premium",
                    new BigDecimal("14.99"),
                    "https://example.com/pizza-mejorada.jpg",
                    true,
                    60,
                    "Pizzas"
            );

            expectedUpdatedProduct = new ProductResponseDto(
                    PRODUCT_ID,
                    RESTAURANT_ID,
                    CATEGORY_ID,
                    "Pizza Margherita Mejorada",
                    "Auténtica pizza italiana con ingredientes premium",
                    new BigDecimal("14.99"),
                    "https://example.com/pizza-mejorada.jpg",
                    true,
                    60,
                    "Pizzas",
                    "Atlántico"
            );
        }

        /**
         * Test que verifica que al actualizar un producto con un ID válido,
         * se retorna el código 200 Ok y los datos del producto actualizado
         * Arrange: Configura el mock del servicio para que retorne el producto actualizado
         * Act & Assert: Realiza la petición PATCH y verifica el status 200 y los datos retornados
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("PATCH /api/product/{id} - Debe retornar 200 OK al actualizar un producto con datos válidos")
        void whenUpdateProductWithValidData_thenReturnsOkWithUpdatedData() throws Exception {
            // Arrange
            when(productService.updateProduct(eq(PRODUCT_ID), any(ProductUpdateDto.class)))
                    .thenReturn(expectedUpdatedProduct);

            // Act & Assert
            mockMvc.perform(patch(PRODUCT_ENDPOINT + "/" + PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.prd_id").value(PRODUCT_ID))
                    .andExpect(jsonPath("$.name").value("Pizza Margherita Mejorada"))
                    .andExpect(jsonPath("$.price").value(14.99))
                    .andExpect(jsonPath("$.quantity").value(60));

            // Verify
            verify(productService, times(1)).updateProduct(eq(PRODUCT_ID), any(ProductUpdateDto.class));
        }

        /**
         * Test que verifica que al actualizar PARCIALMENTE un producto con un ID válido, solo algunos campos,
         * se retorna el código 200 Ok y los datos del producto actualizado
         * Arrange: Configura el mock del servicio para que retorne el producto actualizado
         * Act & Assert: Realiza la petición PATCH y verifica el status 200 y los datos retornados
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("PATCH /api/product/{id} - Debe permitir actualización parcial (solo algunos campos)")
        void whenUpdateProductPartially_thenReturnsOk() throws Exception {
            // Arrange
            ProductUpdateDto partialUpdateDto = new ProductUpdateDto(
                    null,  // Sin cambios en restaurantId
                    CATEGORY_ID,
                    "Pizza Margherita Mejorada",  // Solo se actualiza el nombre
                    null,  // Sin cambios en description
                    null,  // Sin cambios en price
                    null,  // Sin cambios en image
                    null,  // Sin cambios en isActive
                    null,  // Sin cambios en quantity
                    null   // Sin cambios en categoryName
            );

            ProductResponseDto partialResponse = new ProductResponseDto(
                    PRODUCT_ID,
                    RESTAURANT_ID,
                    CATEGORY_ID,
                    "Pizza Margherita Mejorada",
                    "Auténtica pizza italiana con mozzarella fresca",
                    new BigDecimal("12.99"),
                    "https://example.com/pizza.jpg",
                    true,
                    50,
                    "Pizzas",
                    "Atlántico"
            );

            when(productService.updateProduct(eq(PRODUCT_ID), any(ProductUpdateDto.class)))
                    .thenReturn(partialResponse);

            // Act & Assert
            mockMvc.perform(patch(PRODUCT_ENDPOINT + "/" + PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(partialUpdateDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Pizza Margherita Mejorada"));

            verify(productService, times(1)).updateProduct(eq(PRODUCT_ID), any(ProductUpdateDto.class));
        }

        /**
         * Test que verifica que se puede desactivar un producto (isActive = false) con un ID válido,
         * se retorna el código 200 Ok y los datos del producto actualizado
         * Arrange: Configura el mock del servicio para que retorne el producto actualizado
         * Act & Assert: Realiza la petición PATCH y verifica el status 200 y los datos retornados
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("PATCH /api/product/{id} - Debe permitir desactivar un producto (isActive = false)")
        void whenDeactivateProduct_thenReturnsOk() throws Exception {
            // Arrange
            ProductUpdateDto deactivateDto = new ProductUpdateDto(
                    RESTAURANT_ID,
                    CATEGORY_ID,
                    null, null, null, null,
                    false,  // ← Deactivar
                    null, null
            );

            ProductResponseDto deactivatedResponse = new ProductResponseDto(
                    PRODUCT_ID,
                    RESTAURANT_ID,
                    CATEGORY_ID,
                    "Pizza Margherita",
                    "Auténtica pizza italiana",
                    new BigDecimal("12.99"),
                    "https://example.com/pizza.jpg",
                    false,  // ← Inactivo
                    50,
                    "Pizzas",
                    "Atlántico"
            );

            when(productService.updateProduct(eq(PRODUCT_ID), any(ProductUpdateDto.class)))
                    .thenReturn(deactivatedResponse);

            // Act & Assert
            mockMvc.perform(patch(PRODUCT_ENDPOINT + "/" + PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(deactivateDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));

            verify(productService, times(1)).updateProduct(eq(PRODUCT_ID), any(ProductUpdateDto.class));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - PATCH /api/product/{prd_id}")
    class ErrorCases {

        private ProductUpdateDto validUpdateDto;

        @BeforeEach
        void setUp() {
            validUpdateDto = new ProductUpdateDto(
                    RESTAURANT_ID,
                    CATEGORY_ID,
                    "Pizza Margherita Mejorada",
                    "Descripción actualizada",
                    new BigDecimal("14.99"),
                    "https://example.com/pizza-mejorada.jpg",
                    true,
                    60,
                    "Pizzas"
            );
        }

        /**
         * Test que verifica que al intentar actualizar un producto con un ID inexistente,
         * se lanza ProductNotFoundException y se retorna 404 Not Found
         * Arrange: Configura el mock del servicio para que lance ProductNotFoundException
         * Act & Assert: Realiza la petición PATCH y verifica el status 404 y el mensaje de error
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/product/{id} - Debe retornar 404 Not Found si el producto no existe")
        void whenProductNotFound_thenReturns404() throws Exception {
            // Arrange
            when(productService.updateProduct(eq(999L), any(ProductUpdateDto.class)))
                    .thenThrow(new ProductNotFoundException("No se ha encontrado el producto con el ID 999"));

            // Act & Assert
            mockMvc.perform(patch(PRODUCT_ENDPOINT + "/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("ProductNotFoundException"));

            verify(productService, times(1)).updateProduct(eq(999L), any(ProductUpdateDto.class));
        }

        /**
         * Test que verifica que al intentar actualizar un producto con una categoría inexistente,
         * se lanza CategoryNotFoundException y se retorna 404 Not Found
         * Arrange: Configura el mock del servicio para que lance CategoryNotFoundException
         * Act & Assert: Realiza la petición PATCH y verifica el status 404 y el mensaje de error
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/product/{id} - Debe retornar 404 Not Found si la categoría a asignar no existe")
        void whenCategoryNotFound_thenReturns404() throws Exception {
            // Arrange
            when(productService.updateProduct(eq(PRODUCT_ID), any(ProductUpdateDto.class)))
                    .thenThrow(new CategoryNotFoundException("No se ha encontrado la categoria con el ID 999"));

            // Act & Assert
            mockMvc.perform(patch(PRODUCT_ENDPOINT + "/" + PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("CategoryNotFoundException"));

            verify(productService, times(1)).updateProduct(eq(PRODUCT_ID), any(ProductUpdateDto.class));
        }

        /**
         * Test que verifica que al intentar actualizar un producto sin ser el dueño,
         * se lanza UnauthorizedAccessException y se retorna 403 Forbidden
         * Arrange: Configura el mock del servicio para que lance UnauthorizedAccessException
         * Act & Assert: Realiza la petición PATCH y verifica el status 403
         * Verify: Verifica que el servicio se llamó una sola vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/product/{id} - Debe retornar 403 Forbidden si el usuario no es dueño del restaurante")
        void whenUserNotRestaurantOwner_thenReturnsForbidden() throws Exception {
            // Arrange
            when(productService.updateProduct(eq(PRODUCT_ID), any(ProductUpdateDto.class)))
                    .thenThrow(new UnauthorizedAccessException("No tienes permiso para actualizar este producto"));

            // Act & Assert
            mockMvc.perform(patch(PRODUCT_ENDPOINT + "/" + PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(user("otro@example.com").roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));

            verify(productService, times(1)).updateProduct(eq(PRODUCT_ID), any(ProductUpdateDto.class));
        }

        /**
         * Test que verifica que al intentar actualizar un producto sin autenticación, se retorna 401 Unauthorized
         * Act & Assert: Realiza la petición PATCH sin usuario y verifica el status 401
         * Verify: Verifica que el servicio NUNCA se llamó
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/product/{id} - Debe retornar 401 Unauthorized sin autenticación")
        void whenUpdateProductWithoutAuthentication_thenUnauthorized() throws Exception {
            // Act & Assert
            mockMvc.perform(patch(PRODUCT_ENDPOINT + "/" + PRODUCT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateDto))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NUNCA se llamó
            verify(productService, never()).updateProduct(anyLong(), any(ProductUpdateDto.class));
        }
    }
}