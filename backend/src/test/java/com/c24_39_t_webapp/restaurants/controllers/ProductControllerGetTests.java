package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.response.GroupedProductsResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductSummaryResponseDto;
import com.c24_39_t_webapp.restaurants.exception.ProductNotFoundException;
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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para el endpoint de obtener productos - Endpoint Get /api/product/
 * Verifica que tras obtener productos con un ID válido, se retorna el código 200 OK
 * <p>
 * También verifica los casos de error:
 * - RestaurantNotFoundException: cuando el producto no existe. Retorna 404 Not Found
 * - UnauthorizedAccessException: cuando el usuario no tiene permiso. Retorna 403 Forbidden
 * - Petición sin autenticación: retorna 401 Unauthorized
 * * Patrón AAA: Arrange, Act, Assert
 * Cobertura:
 * ✅ GET /api/product (todos los productos)
 * ✅ GET /api/product/{prd_id} (producto por ID)
 * ✅ GET /api/product/byCategory/{categoryId} (productos por categoría)
 * ✅ GET /api/product/byName (productos por nombre)
 * ✅ GET /api/product/byRestaurant/{restaurantId} (productos por restaurante)
 * ✅ GET /api/product/byRestaurantAndCategory/{restaurantId} (productos agrupados por categoría)
 */
@WebMvcTest(
        controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@Slf4j
@DisplayName("ProductController - GET /api/product/*")
public class ProductControllerGetTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String PRODUCT_ENDPOINT = "/api/product";
    private static final String CLIENTE_EMAIL = "cliente@example.com";
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

    // ==================== GET ALL PRODUCTS ====================

    @Nested
    @DisplayName("GET /api/product (Get All Products)")
    class GetAllProductsTests {

        private List<ProductResponseDto> mockProductsList;

        @BeforeEach
        void setUp() {
            mockProductsList = List.of(
                    new ProductResponseDto(
                            1L, RESTAURANT_ID, CATEGORY_ID, "Pizza Margherita",
                            "Clásica italiana", new BigDecimal("12.99"),
                            "https://example.com/pizza1.jpg", true, 50,
                            "Pizzas", "Atlántico"
                    ),
                    new ProductResponseDto(
                            2L, RESTAURANT_ID, 2L, "Pasta Carbonara",
                            "Receta romana", new BigDecimal("13.50"),
                            "https://example.com/pasta.jpg", true, 40,
                            "Pastas", "Atlántico"
                    )
            );
        }

        /**
         * Test que verifica que al obtener todos los productos, retorna el codigo 200 Ok con la lista de productos
         * Arrange: Configura el mock del servicio para retornar la lista de productos
         * Act & Assert: Realiza la petición GET y verifica el status 200 y el contenido del JSON
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET All /api/product Debe retornar 200 OK con lista de todos los productos")
        void whenGetAllProducts_thenReturnsOkWithList() throws Exception {
            // Arrange
            when(productService.findAllProducts()).thenReturn(mockProductsList);

            // Act & Assert
            mockMvc.perform(get(PRODUCT_ENDPOINT)
                            .with(user(CLIENTE_EMAIL).roles("CLIENTE")))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].prd_id").value(1L))
                    .andExpect(jsonPath("$[0].name").value("Pizza Margherita"))
                    .andExpect(jsonPath("$[1].prd_id").value(2L))
                    .andExpect(jsonPath("$[1].name").value("Pasta Carbonara"));

            verify(productService, times(1)).findAllProducts();
        }

        /**
         * Test que verifica que al obtener todos los productos y no los hay, retorna el codigo 200 Ok con una lista vacía
         * Arrange: Configura el mock del servicio para retornar la lista de productos
         * Act & Assert: Realiza la petición GET y verifica el status 200 y el contenido del JSON
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET All /api/product  Debe retornar 200 OK con lista VACÍA si no hay productos")
        void whenNoProducts_thenReturnsEmptyList() throws Exception {
            // Arrange
            when(productService.findAllProducts()).thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get(PRODUCT_ENDPOINT)
                            .with(user(CLIENTE_EMAIL).roles("CLIENTE")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(productService, times(1)).findAllProducts();
        }
    }

    // ==================== GET PRODUCT BY ID ====================

    @Nested
    @DisplayName("GET /api/product/{prd_id} (Get Product By ID)")
    class GetProductByIdTests {

        private ProductResponseDto expectedProduct;

        @BeforeEach
        void setUp() {
            expectedProduct = new ProductResponseDto(
                    PRODUCT_ID, RESTAURANT_ID, CATEGORY_ID, "Pizza Margherita",
                    "Clásica italiana", new BigDecimal("12.99"),
                    "https://example.com/pizza.jpg", true, 50,
                    "Pizzas", "Atlántico"
            );
        }

        @Nested
        @DisplayName("GetByID Success Cases")
        class GetByIdSuccessCases {
            /**
             * Test que verifica que al obtener un producto con un ID válido retorna el codigo 200 Ok con los datos del producto
             * Arrange: Configura el mock del servicio para retornar el producto esperado
             * Act & Assert: Realiza la petición GET y verifica el status 200 y el contenido del JSON
             * Verify: Verifica que el servicio se llamó una sola vez
             *
             * @throws Exception
             */
            @Test
            @DisplayName("GetByID /api/product/{prd_id} - Debe retornar 200 OK con datos del producto")
            void whenGetProductByValidId_thenReturnsOk() throws Exception {
                // Arrange
                when(productService.findProductById(PRODUCT_ID))
                        .thenReturn(expectedProduct);

                // Act & Assert
                mockMvc.perform(get(PRODUCT_ENDPOINT + "/" + PRODUCT_ID)
                                .with(user(CLIENTE_EMAIL).roles("CLIENTE")))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.prd_id").value(PRODUCT_ID))
                        .andExpect(jsonPath("$.name").value("Pizza Margherita"))
                        .andExpect(jsonPath("$.price").value(12.99))
                        .andExpect(jsonPath("$.isActive").value(true));

                verify(productService, times(1)).findProductById(PRODUCT_ID);
            }
        }

        @Nested
        @DisplayName("GetByID Error Cases")
        class GetByIdErrorCases {
            /**
             * Test que verifica que al obtener un producto con un ID inválido retorna el codigo 404 Not Found
             * Arrange: Configura el mock del servicio para lanzar ProductNotFoundException
             * Act & Assert: Realiza la petición GET y verifica el status 404 y el mensaje de error
             * Verify: Verifica que el servicio se llamó una sola vez
             *
             * @throws Exception
             */
            @Test
            @DisplayName("Fail GetByID /api/product/{prd_id} Debe retornar 404 Not Found si el producto no existe")
            void whenProductNotFound_thenReturns404() throws Exception {
                // Arrange
                when(productService.findProductById(999L))
                        .thenThrow(new ProductNotFoundException("No se encontro una producto con ese ID: 999"));

                // Act & Assert
                mockMvc.perform(get(PRODUCT_ENDPOINT + "/999")
                                .with(user(CLIENTE_EMAIL).roles("CLIENTE")))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error").value("ProductNotFoundException"));

                verify(productService, times(1)).findProductById(999L);
            }
        }
        // ==================== GET PRODUCTS BY CATEGORY ====================

        @Nested
        @DisplayName("GET /api/product/byCategory/{categoryId}")
        class GetProductsByCategoryTests {

            private List<ProductSummaryResponseDto> mockProductsList;

            @BeforeEach
            void setUp() {
                mockProductsList = List.of(
                        new ProductSummaryResponseDto(
                                1L, RESTAURANT_ID, CATEGORY_ID, "Pizza Margherita",
                                "Clásica italiana", "https://example.com/pizza1.jpg"
                        ),
                        new ProductSummaryResponseDto(
                                2L, RESTAURANT_ID, CATEGORY_ID, "Pizza Quattro Formaggi",
                                "Cuatro quesos", "https://example.com/pizza2.jpg"
                        )
                );
            }

            /**
             * Test que verifica que al obtener un producto con un ID inválido retorna el codigo 404 Not Found
             * Arrange: Configura el mock del servicio para lanzar ProductNotFoundException
             * Act & Assert: Realiza la petición GET y verifica el status 404 y el mensaje de error
             * Verify: Verifica que el servicio se llamó una sola vez
             *
             * @throws Exception
             */
            /**
             * Test que verifica que al obtener todos los productos por categoría, retorna el codigo 200 Ok
             * con la lista de productos
             * Arrange: Configura el mock del servicio para retornar la lista de productos
             * Act & Assert: Realiza la petición GET y verifica el status 200 y el contenido del JSON
             * Verify: Verifica que el servicio se llamó una sola vez
             *
             * @throws Exception
             */
            @Test
            @DisplayName("Get By Category /api/product/byCategory/{categoryId} Debe retornar 200 OK con lista de productos de la categoría")
            void whenGetProductsByCategory_thenReturnsOk() throws Exception {
                // Arrange
                when(productService.findProductsByCategoryId(CATEGORY_ID))
                        .thenReturn(mockProductsList);

                // Act & Assert
                mockMvc.perform(get(PRODUCT_ENDPOINT + "/byCategory/" + CATEGORY_ID)
                                .with(user(CLIENTE_EMAIL).roles("CLIENTE")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].name").value("Pizza Margherita"))
                        .andExpect(jsonPath("$[1].name").value("Pizza Quattro Formaggi"));

                verify(productService, times(1)).findProductsByCategoryId(CATEGORY_ID);
            }

            /**
             * Test que verifica que al obtener todos los productos por categoría y no los hay,
             * retorna el codigo 200 Ok con una lista vacía
             * Arrange: Configura el mock del servicio para retornar la lista de productos vacía
             * Act & Assert: Realiza la petición GET y verifica el status 200 y el contenido del JSON
             * Verify: Verifica que el servicio se llamó una sola vez
             *
             * @throws Exception
             */
            @Test
            @DisplayName("Get By Category /api/product/byCategory/{categoryId} Debe retornar 200 OK con lista VACÍA si la categoría no tiene productos")
            void whenCategoryHasNoProducts_thenReturnsEmptyList() throws Exception {
                // Arrange
                when(productService.findProductsByCategoryId(999L))
                        .thenReturn(Collections.emptyList());

                // Act & Assert
                mockMvc.perform(get(PRODUCT_ENDPOINT + "/byCategory/999")
                                .with(user(CLIENTE_EMAIL).roles("CLIENTE")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(0)));

                verify(productService, times(1)).findProductsByCategoryId(999L);
            }
        }

        // ==================== GET PRODUCTS BY NAME ====================

        @Nested
        @DisplayName("GET /api/product/byName (Query Parameter)")
        class GetProductsByNameTests {

            private List<ProductSummaryResponseDto> mockProductsList;

            @BeforeEach
            void setUp() {
                mockProductsList = List.of(
                        new ProductSummaryResponseDto(
                                1L, RESTAURANT_ID, CATEGORY_ID, "Pizza Margherita",
                                "Clásica italiana", "https://example.com/pizza.jpg"
                        )
                );
            }

            /**
             * Test que verifica que al obtener un producto con un nombre válido retorna el código 200 Ok
             * con una lista de productos que coincidan
             * Arrange: Configura el mock del servicio para retornar el producto esperado
             * Act & Assert: Realiza la petición GET y verifica el status 200 y el contenido del JSON
             * Verify: Verifica que el servicio se llamó una sola vez
             *
             * @throws Exception
             */
            @Test
            @DisplayName("GET by Name /api/product/byName  Debe retornar 200 OK con productos que coinciden con el nombre")
            void whenGetProductsByName_thenReturnsOk() throws Exception {
                // Arrange
                when(productService.findProductsByName("Pizza"))
                        .thenReturn(mockProductsList);

                // Act & Assert
                mockMvc.perform(get(PRODUCT_ENDPOINT + "/byName")
                                .param("name", "Pizza")
                                .with(user(CLIENTE_EMAIL).roles("CLIENTE")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(1)))
                        .andExpect(jsonPath("$[0].name").value("Pizza Margherita"));

                verify(productService, times(1)).findProductsByName("Pizza");
            }

            /**
             * Test que verifica que al obtener los productos con un nombre válido retorna el codigo 200 Ok
             * con una lista vacía si no hay coincidencias
             * Arrange: Configura el mock del servicio para retornar una lista vacía
             * Act & Assert: Realiza la petición GET y verifica el status 200 y el contenido del JSON
             * Verify: Verifica que el servicio se llamó una sola vez
             *
             * @throws Exception
             */
            @Test
            @DisplayName("GET by Name /api/product/byName Debe retornar 200 OK con lista VACÍA si no hay coincidencias")
            void whenNameNotFound_thenReturnsEmptyList() throws Exception {
                // Arrange
                when(productService.findProductsByName("NoExiste"))
                        .thenReturn(Collections.emptyList());

                // Act & Assert
                mockMvc.perform(get(PRODUCT_ENDPOINT + "/byName")
                                .param("name", "NoExiste")
                                .with(user(CLIENTE_EMAIL).roles("CLIENTE")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(0)));

                verify(productService, times(1)).findProductsByName("NoExiste");
            }
        }

        // ==================== GET PRODUCTS BY RESTAURANT ====================

        @Nested
        @DisplayName("GET /api/product/byRestaurant/{restaurantId}")
        class GetProductsByRestaurantTests {

            private List<ProductResponseDto> mockProductsList;

            @BeforeEach
            void setUp() {
                mockProductsList = List.of(
                        new ProductResponseDto(
                                1L, RESTAURANT_ID, 1L, "Pizza Margherita",
                                "Clásica italiana", new BigDecimal("12.99"),
                                "https://example.com/pizza1.jpg", true, 50,
                                "Pizzas", "Atlántico"
                        ),
                        new ProductResponseDto(
                                2L, RESTAURANT_ID, 2L, "Pasta Carbonara",
                                "Receta romana", new BigDecimal("13.50"),
                                "https://example.com/pasta.jpg", true, 40,
                                "Pastas", "Atlántico"
                        )
                );
            }

            /**
             * Test que verifica que al obtener los productos para un restaurante válido retorna el codigo 200 Ok
             * con una lista de productos de ese restaurante
             * Arrange: Configura el mock del servicio para retornar la lista de productos
             * Act & Assert: Realiza la petición GET y verifica el status 200 y el contenido del JSON
             * Verify: Verifica que el servicio se llamó una sola vez
             *
             * @throws Exception
             */
            @Test
            @DisplayName(" GET by Restaurant /api/product/byRestaurant Debe retornar 200 OK con lista de productos del restaurante")
            void whenGetProductsByRestaurant_thenReturnsOk() throws Exception {
                // Arrange
                when(productService.findProductsByRestaurantId(RESTAURANT_ID))
                        .thenReturn(mockProductsList);

                // Act & Assert
                mockMvc.perform(get(PRODUCT_ENDPOINT + "/byRestaurant/" + RESTAURANT_ID)
                                .with(user(CLIENTE_EMAIL).roles("CLIENTE")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].restaurantName").value("Atlántico"))
                        .andExpect(jsonPath("$[1].restaurantName").value("Atlántico"));

                verify(productService, times(1)).findProductsByRestaurantId(RESTAURANT_ID);
            }

            /**
             * Test que verifica que al obtener los productos de un restaurante válido sin productos retorna el codigo 200 Ok
             * con una lista vacía si no hay coincidencias
             * Arrange: Configura el mock del servicio para retornar una lista vacía
             * Act & Assert: Realiza la petición GET y verifica el status 200 y el contenido del JSON
             * Verify: Verifica que el servicio se llamó una sola vez
             *
             * @throws Exception
             */
            @Test
            @DisplayName(" GET by Restaurant /api/product/byRestaurant Debe retornar 200 OK con lista VACÍA si el restaurante no tiene productos")
            void whenRestaurantHasNoProducts_thenReturnsEmptyList() throws Exception {
                // Arrange
                when(productService.findProductsByRestaurantId(999L))
                        .thenReturn(Collections.emptyList());

                // Act & Assert
                mockMvc.perform(get(PRODUCT_ENDPOINT + "/byRestaurant/999")
                                .with(user(CLIENTE_EMAIL).roles("CLIENTE")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(0)));

                verify(productService, times(1)).findProductsByRestaurantId(999L);
            }
        }

        // ==================== GET PRODUCTS BY RESTAURANT AND CATEGORY (GROUPED) ====================

        @Nested
        @DisplayName("GET /api/product/byRestaurantAndCategory/{restaurantId} (Grouped)")
        class GetProductsByRestaurantAndCategoryTests {

            private List<GroupedProductsResponseDto> mockGroupedProducts;

            @BeforeEach
            void setUp() {
                List<ProductResponseDto> pizzasProducts = List.of(
                        new ProductResponseDto(
                                1L, RESTAURANT_ID, 1L, "Pizza Margherita",
                                "Clásica italiana", new BigDecimal("12.99"),
                                "https://example.com/pizza1.jpg", true, 50,
                                "Pizzas", "Atlántico"
                        ),
                        new ProductResponseDto(
                                2L, RESTAURANT_ID, 1L, "Pizza Quattro Formaggi",
                                "Cuatro quesos", new BigDecimal("14.99"),
                                "https://example.com/pizza2.jpg", true, 40,
                                "Pizzas", "Atlántico"
                        )
                );

                List<ProductResponseDto> pastasProducts = List.of(
                        new ProductResponseDto(
                                3L, RESTAURANT_ID, 2L, "Pasta Carbonara",
                                "Receta romana", new BigDecimal("13.50"),
                                "https://example.com/pasta.jpg", true, 40,
                                "Pastas", "Atlántico"
                        )
                );

                mockGroupedProducts = List.of(
                        new GroupedProductsResponseDto(
                                "Pizzas", 1L, "Atlántico", RESTAURANT_ID, pizzasProducts
                        ),
                        new GroupedProductsResponseDto(
                                "Pastas", 2L, "Atlántico", RESTAURANT_ID, pastasProducts
                        )
                );
            }

            /**
             * Test que verifica que al obtener los productos para un restaurante válido, agrupados por categoría,
             * retorna el codigo 200 Ok con una lista de productos de ese restaurante agrupados por categoría
             * Arrange: Configura el mock del servicio para retornar la lista de productos
             * Act & Assert: Realiza la petición GET y verifica el status 200 y el contenido del JSON
             * Verify: Verifica que el servicio se llamó una sola vez
             *
             * @throws Exception
             */
            @Test
            @DisplayName("GET by Restaurant & Category Debe retornar 200 OK con productos agrupados por categoría")
            void whenGetProductsGroupedByRestaurantAndCategory_thenReturnsOk() throws Exception {
                // Arrange
                when(productService.findProductsByRestaurantIdAndCategory(RESTAURANT_ID))
                        .thenReturn(mockGroupedProducts);

                // Act & Assert
                mockMvc.perform(get(PRODUCT_ENDPOINT + "/byRestaurantAndCategory/" + RESTAURANT_ID)
                                .with(user(CLIENTE_EMAIL).roles("CLIENTE")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(2)))
                        .andExpect(jsonPath("$[0].categoryName").value("Pizzas"))
                        .andExpect(jsonPath("$[0].categoryId").value(1L))
                        .andExpect(jsonPath("$[0].restaurantName").value("Atlántico"))
                        .andExpect(jsonPath("$[0].products", hasSize(2)))
                        .andExpect(jsonPath("$[0].products[0].name").value("Pizza Margherita"))
                        .andExpect(jsonPath("$[0].products[1].name").value("Pizza Quattro Formaggi"))
                        .andExpect(jsonPath("$[1].categoryName").value("Pastas"))
                        .andExpect(jsonPath("$[1].categoryId").value(2L))
                        .andExpect(jsonPath("$[1].products", hasSize(1)))
                        .andExpect(jsonPath("$[1].products[0].name").value("Pasta Carbonara"));

                verify(productService, times(1)).findProductsByRestaurantIdAndCategory(RESTAURANT_ID);
            }

            /**
             * Test que verifica que al obtener los productos para un restaurante válido, agrupados por categoría,
             * si no los hay, retorna el codigo 200 Ok con una lista de productos de ese restaurante vacía
             * Arrange: Configura el mock del servicio para retornar la lista de productos vacía
             * Act & Assert: Realiza la petición GET y verifica el status 200 y el contenido del JSON
             * Verify: Verifica que el servicio se llamó una sola vez
             *
             * @throws Exception
             */
            @Test
            @DisplayName("GET by Restaurant & Category Debe retornar 200 OK con lista VACÍA si el restaurante no tiene productos")
            void whenRestaurantHasNoGroupedProducts_thenReturnsEmptyList() throws Exception {
                // Arrange
                when(productService.findProductsByRestaurantIdAndCategory(999L))
                        .thenReturn(Collections.emptyList());

                // Act & Assert
                mockMvc.perform(get(PRODUCT_ENDPOINT + "/byRestaurantAndCategory/999")
                                .with(user(CLIENTE_EMAIL).roles("CLIENTE")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(0)));

                verify(productService, times(1)).findProductsByRestaurantIdAndCategory(999L);
            }
        }
    }
}