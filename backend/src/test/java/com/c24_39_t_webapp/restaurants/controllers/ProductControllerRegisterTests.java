package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.ProductRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.ProductUpdateDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.ProductFactory;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para ProductController - Endpoint POST (Create/Register)
 * Arquitectura aplicada:
 * - @WebMvcTest: Solo carga ProductController (slice testing)
 * - @MockitoBean: Mockea IProductService
 * - @Nested: Agrupa Success Cases y Error Cases
 * - AAA Pattern: Arrange, Act, Assert en cada test
 * - Patrón Given-When-Then implícito en los tests
 * - Uso de constantes para evitar "magic strings"
 * Cobertura:
 * ✅ POST /api/product con datos válidos → 201 Created
 * ✅ POST /api/product con restaurante no encontrado → 404 Not Found
 * ✅ POST /api/product con categoría no encontrada → 404 Not Found
 * ✅ POST /api/product sin permiso (no es dueño del restaurante) → 403 Forbidden
 * ✅ POST /api/product sin autenticación → 401 Unauthorized
 */
@WebMvcTest(
        controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@Slf4j
@DisplayName("ProductController - POST /api/product (Register/Create)")
public class ProductControllerRegisterTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String PRODUCT_ENDPOINT = "/api/product";
    private static final String RESTAURANTE_EMAIL = "restaurante@example.com";
    private static final long RESTAURANT_ID = 1L;
    private static final long CATEGORY_ID = 1L;
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
    @DisplayName("Success Cases - POST /api/product")
    class SuccessCases {

        private ProductRequestDto validProductDto;
        private ProductResponseDto expectedProductResponse;

        @BeforeEach
        void setUp() {
            // Obtiene desde la factory el request por defecto
            validProductDto = ProductFactory.defaultProductRequest(RESTAURANT_ID);

            // expected response generado por la factory (mapea request -> response y asigna id)
            expectedProductResponse = ProductFactory.responseFromRequest(validProductDto, PRODUCT_ID);
        }

        /**
         * Test que verifica que al registrar un producto con datos válidos, se retorna el código 201 Created
         * y los datos del producto creado.
         * Arrange: Configura el mock del servicio para retornar el producto esperado
         * Act & Assert: Realiza la petición POST y verifica el status 201 y el contenido del JSON
         * Verify: Verifica que el servicio se llamó una vez con los datos correctos
         *
         * @throws Exception
         */
        @Test
        @DisplayName("POST /api/product - Debe retornar 201 Created al crear un producto con datos válidos")
        void whenCreateProductWithValidData_thenReturnsCreatedWithProductData() throws Exception {
            // Arrange
            when(productService.addProduct(any(ProductRequestDto.class)))
                    .thenReturn(expectedProductResponse);

            // Act & Assert
            mockMvc.perform(post(PRODUCT_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validProductDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.prd_id").value(PRODUCT_ID))
                    .andExpect(jsonPath("$.name").value("Pizza Margherita"))
                    .andExpect(jsonPath("$.price").value(14.99))
                    .andExpect(jsonPath("$.restaurantName").value("Mediterráneo"));

            // Verify
            verify(productService, times(1)).addProduct(any(ProductRequestDto.class));
        }

        /**
         * Test que verifica que al registrar un producto, se hace con la opción isActive = false
         * Arrange: Configura el mock del servicio para retornar el producto inactivo
         * Act & Assert: Realiza la petición POST y verifica el status 201 y el contenido del JSON
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("POST /api/product - Debe permitir crear un producto inactivo (isActive = false)")
        void whenCreateInactiveProduct_thenReturnsCreated() throws Exception {
            // Arrange
            // Crea un producto base activo
            ProductUpdateDto response = ProductFactory.defaultUpdatedProduct(PRODUCT_ID, "Pizza Margherita", "Auténtica pizza italiana");
            // Lo convierte en inactivo
            ProductUpdateDto inactiveProductDto = ProductFactory.updatedFrom(response, RESTAURANT_ID, response.name(), response.description(), false);
            // response generado a partir del request inactivo
            ProductResponseDto inactiveResponse = ProductFactory.responseFromUpdate(inactiveProductDto, PRODUCT_ID);

            when(productService.addProduct(any(ProductRequestDto.class)))
                    .thenReturn(inactiveResponse);

            // Act & Assert
            mockMvc.perform(post(PRODUCT_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inactiveProductDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.isActive").value(false));

            verify(productService, times(1)).addProduct(any(ProductRequestDto.class));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - POST /api/product")
    class ErrorCases {

        private ProductRequestDto validProductDto;

        @BeforeEach
        void setUp() {
            // Obtiene desde la factory un request válido base
            validProductDto = ProductFactory.defaultProductRequest(RESTAURANT_ID);
        }

        /**
         * Test que verifica que si el restaurante no existe se retorna un 404 Not Found
         * Arrange: Configura el mock del servicio para que lance RestaurantNotFoundException
         * Act & Assert: Realiza la petición POST y verifica el status 404 y el mensaje de error
         * Verify: Verifica que el servicio se llamó una vez con los datos correctos
         * @throws Exception
         */
        @Test
        @DisplayName("Fail POST /api/product - Debe retornar 404 Not Found si el restaurante no existe")
        void whenRestaurantNotFound_thenReturns404() throws Exception {
            // Arrange
            when(productService.addProduct(any(ProductRequestDto.class)))
                    .thenThrow(new RestaurantNotFoundException("No se ha encontrado el restaurante con ID: " + RESTAURANT_ID));

            // Act & Assert
            mockMvc.perform(post(PRODUCT_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validProductDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("RestaurantNotFoundException"))
                    .andExpect(jsonPath("$.message").exists());

            verify(productService, times(1)).addProduct(any(ProductRequestDto.class));
        }

        /**
         * Test que verifica que si la categoría no existe se retorna un 404 Not Found
         * Arrange: Configura el mock del servicio para que lance CategoryNotFoundException
         * Act & Assert: Realiza la petición POST y verifica el status 404 y el mensaje de error
         * Verify: Verifica que el servicio se llamó una vez con los datos correctos
         * @throws Exception
         */
        @Test
        @DisplayName("Debe retornar 404 Not Found si la categoría no existe")
        void whenCategoryNotFound_thenReturns404() throws Exception {
            // Arrange
            when(productService.addProduct(any(ProductRequestDto.class)))
                    .thenThrow(new CategoryNotFoundException("No se ha encontrado la categoría con ID: " + CATEGORY_ID));

            // Act & Assert
            mockMvc.perform(post(PRODUCT_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validProductDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("CategoryNotFoundException"));

            verify(productService, times(1)).addProduct(any(ProductRequestDto.class));
        }

        /**
         * Test que verifica que al intentar registrar un producto sin ser el dueño,
         * se lanza UnauthorizedAccessException y se retorna 403 Forbidden
         * Arrange: Configura el mock del servicio para que lance UnauthorizedAccessException
         * Act & Assert: Realiza la petición POST y verifica el status 403
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         * @throws Exception
         */
        @Test
        @DisplayName("Debe retornar 403 Forbidden si el usuario no es dueño del restaurante")
        void whenUserNotRestaurantOwner_thenReturnsForbidden() throws Exception {
            // Arrange
            when(productService.addProduct(any(ProductRequestDto.class)))
                    .thenThrow(new UnauthorizedAccessException("No tienes permiso para añadir productos a este restaurante"));

            // Act & Assert
            mockMvc.perform(post(PRODUCT_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validProductDto))
                            .with(user("otro@example.com").roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));

            verify(productService, times(1)).addProduct(any(ProductRequestDto.class));
        }

        /**
         * Test que verifica que al intentar registrar un producto sin autenticación, se retorna 401 Unauthorized
         * Act & Assert: Realiza la petición POST sin usuario y verifica el status 401
         * Verify: Verifica que el servicio NUNCA se llamó
         * @throws Exception
         */
        @Test
        @DisplayName("Debe retornar 401 Unauthorized sin autenticación")
        void whenCreateProductWithoutAuthentication_thenUnauthorized() throws Exception {
            // Act & Assert
            mockMvc.perform(post(PRODUCT_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validProductDto))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NUNCA se llamó
            verify(productService, never()).addProduct(any(ProductRequestDto.class));
        }
        /**
         * Test que verifica que al intentar registrar un producto con restaurante nulo, se retorna 400 Bad Request
         * Act & Assert: Realiza la petición POST con restaurante id nulo y verifica el status 400
         * Verify: Verifica que el servicio NUNCA se llamó
         * @throws Exception
         */
        @Test
        @DisplayName("Debe retornar 400 Bad Request si restaurantId es nulo")
        void whenRestaurantIdIsNull_thenReturnsBadRequest() throws Exception {
            // Arrange
            ProductRequestDto restaurantIdInvalidDto = ProductFactory.productRequestFromDefault(
                    RESTAURANT_ID,
                    base -> new ProductRequestDto(
                            null,
                            base.categoryId(),
                            base.name(),
                            base.description(),
                            base.price(),
                            base.image(),
                            base.isActive(),
                            base.quantity()
                    )
            );

            // Act & Assert
            mockMvc.perform(post(PRODUCT_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(restaurantIdInvalidDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validación fallida"));

            verify(productService, never()).addProduct(any(ProductRequestDto.class));
        }

        /**
         * Test que verifica que al intentar registrar un producto con category nula, se retorna 400 Bad Request
         * Act & Assert: Realiza la petición POST con category id nula y verifica el status 400
         * Verify: Verifica que el servicio NUNCA se llamó
         * @throws Exception
         */
        @Test
        @DisplayName("Debe retornar 400 Bad Request si categoryId es nulo")
        void whenCategoryIdIsNull_thenReturnsBadRequest() throws Exception {
            // Arrange
            ProductRequestDto categoryIdInvalidDto = ProductFactory.productRequestFromDefault(
                    RESTAURANT_ID,
                    base -> new ProductRequestDto(
                            base.restaurantId(),
                            null,
                            base.name(),
                            base.description(),
                            base.price(),
                            base.image(),
                            base.isActive(),
                            base.quantity()
                    )
            );

            // Act & Assert
            mockMvc.perform(post(PRODUCT_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(categoryIdInvalidDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(productService, never()).addProduct(any(ProductRequestDto.class));
        }

        /**
         * Test que verifica que al intentar registrar un producto sin nombre, se retorna 400 Bad Request
         * Act & Assert: Realiza la petición POST con name nulo y verifica el status 400
         * Verify: Verifica que el servicio NUNCA se llamó
         * @throws Exception
         */
        @Test
        @DisplayName("Debe retornar 400 Bad Request si name está vacío")
        void whenNameIsBlank_thenReturnsBadRequest() throws Exception {
            // Arrange
            ProductRequestDto nameInvalidDto = ProductFactory.productRequestFromDefault(
                    RESTAURANT_ID,
                    base -> new ProductRequestDto(
                            base.restaurantId(),
                            base.categoryId(),
                            null,
                            base.description(),
                            base.price(),
                            base.image(),
                            base.isActive(),
                            base.quantity()
                    )
            );

            // Act & Assert
            mockMvc.perform(post(PRODUCT_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nameInvalidDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(productService, never()).addProduct(any(ProductRequestDto.class));
        }

        /**
         * Test que verifica que al intentar registrar un producto sin precio, se retorna 400 Bad Request
         * Act & Assert: Realiza la petición POST con precio nulo y verifica el status 400
         * Verify: Verifica que el servicio NUNCA se llamó
         * @throws Exception
         */
        @Test
        @DisplayName("Debe retornar 400 Bad Request si price es nulo")
        void whenPriceIsNull_thenReturnsBadRequest() throws Exception {
            // Arrange
            ProductRequestDto priceInvalidDto = ProductFactory.productRequestFromDefault(
                    RESTAURANT_ID,
                    base -> new ProductRequestDto(
                            base.restaurantId(),
                            base.categoryId(),
                            base.name(),
                            base.description(),
                            null,
                            base.image(),
                            base.isActive(),
                            base.quantity()
                    )
            );

            // Act & Assert
            mockMvc.perform(post(PRODUCT_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(priceInvalidDto))
                            .with(user(RESTAURANTE_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(productService, never()).addProduct(any(ProductRequestDto.class));
        }
    }
}