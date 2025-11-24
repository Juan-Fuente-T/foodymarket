package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductSummaryResponseDto;
import com.c24_39_t_webapp.restaurants.exception.ProductNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.models.Product;
import com.c24_39_t_webapp.restaurants.repository.ProductRepository;
import com.c24_39_t_webapp.restaurants.services.impl.ProductServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para métodos find* de ProductServiceImpl
 *
 * Cobertura:
 * ✅ findAllProducts() - lista vacía, lista con datos
 * ✅ findProductById() - producto encontrado, no encontrado, ID inválido
 * ✅ findProductsByCategoryId() - categoría con productos, sin productos
 * ✅ findProductsByName() - búsqueda exitosa, nombre vacío, nombre corto
 * ✅ findProductsByRestaurantId() - restaurante con productos, sin productos
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("ProductServiceImpl - Find Methods Unit Tests")
class ProductServiceFindUnitTests {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private static final long PRODUCT_ID = 1L;
    private static final long CATEGORY_ID = 1L;
    private static final long RESTAURANT_ID = 1L;

    private Product mockProduct;
    private List<Product> mockProductList;

    @BeforeEach
    void setUp() {
        log.info("Setting up test data for find methods");

        mockProduct = EntityModelFactory.defaultProduct();
    }

    // ==================== findAllProducts() ====================

    @Nested
    @DisplayName("findAllProducts()")
    class FindAllProductsTests {

        /**
         * Test que verifica que retorna lista vacía cuando no hay productos.
         */
        @Test
        @DisplayName("findAllProducts() - Retorna lista vacía cuando BD vacía")
        void whenFindAllProductsWithEmptyDatabase_thenReturnsEmptyList() {
            // Arrange
            when(productRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<ProductResponseDto> result = productService.findAllProducts();

            // Assert
            assertNotNull(result, "Lista no debe ser null");
            assertTrue(result.isEmpty(), "Lista debe estar vacía");

            // Verify
            verify(productRepository, times(1)).findAll();
        }

        /**
         * Test que verifica que retorna lista con productos.
         */
        @Test
        @DisplayName("findAllProducts() - Retorna lista con todos los productos")
        void whenFindAllProductsWithData_thenReturnsAllProducts() {
            // Arrange
            mockProductList = EntityModelFactory.defaultProductList();
            when(productRepository.findAll()).thenReturn(mockProductList);

            // Act
            List<ProductResponseDto> result = productService.findAllProducts();

            // Assert
            assertNotNull(result, "Lista no debe ser null");
            assertEquals(2, result.size(), "Debe haber 2 productos");
            assertEquals(mockProduct.getName(), result.get(0).name(), "Nombre debe coincidir");

            // Verify
            verify(productRepository, times(1)).findAll();
        }
    }

    // ==================== findProductById() ====================

    @Nested
    @DisplayName("findProductById()")
    class FindProductByIdTests {

        /**
         * Test que verifica que encuentra producto válido.
         */
        @Test
        @DisplayName("findProductById() - Retorna producto cuando existe")
        void whenFindProductByIdWithValidId_thenReturnsProduct() {
            // Arrange
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));

            // Act
            ProductResponseDto result = productService.findProductById(PRODUCT_ID);

            // Assert
            assertNotNull(result, "Producto no debe ser null");
            assertEquals(PRODUCT_ID, result.prd_id(), "ID debe coincidir");
            assertEquals(mockProduct.getName(), result.name(), "Nombre debe coincidir");

            // Verify
            verify(productRepository, times(1)).findById(PRODUCT_ID);
        }

        /**
         * Test que verifica que lanza excepción para ID no encontrado.
         */
        @Test
        @DisplayName("findProductById() - Lanza ProductNotFoundException si no existe")
        void whenFindProductByIdNotFound_thenThrowsProductNotFoundException() {
            // Arrange
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProductNotFoundException.class,
                    () -> productService.findProductById(PRODUCT_ID));

            // Verify
            verify(productRepository, times(1)).findById(PRODUCT_ID);
        }

        /**
         * Test que verifica que lanza excepción para ID inválido (null).
         */
        @Test
        @DisplayName("findProductById() - Lanza ProductNotFoundException para ID null")
        void whenFindProductByIdWithNullId_thenThrowsProductNotFoundException() {
            // Act & Assert
            assertThrows(ProductNotFoundException.class,
                    () -> productService.findProductById(null));
        }

        /**
         * Test que verifica que lanza excepción para ID negativo.
         */
        @Test
        @DisplayName("findProductById() - Lanza ProductNotFoundException para ID negativo")
        void whenFindProductByIdWithNegativeId_thenThrowsProductNotFoundException() {
            // Act & Assert
            assertThrows(ProductNotFoundException.class,
                    () -> productService.findProductById(-1L));

            // Verify - No debe consultar repositorio
            verify(productRepository, never()).findById(any());
        }

        /**
         * Test que verifica que lanza excepción para ID cero.
         */
        @Test
        @DisplayName("findProductById() - Lanza ProductNotFoundException para ID cero")
        void whenFindProductByIdWithZeroId_thenThrowsProductNotFoundException() {
            // Act & Assert
            assertThrows(ProductNotFoundException.class,
                    () -> productService.findProductById(0L));

            // Verify
            verify(productRepository, never()).findById(any());
        }
    }

    // ==================== findProductsByName() ====================

    @Nested
    @DisplayName("findProductsByName()")
    class FindProductsByNameTests {

        /**
         * Test que verifica búsqueda exitosa por nombre.
         */
        @Test
        @DisplayName("findProductsByName() - Retorna productos encontrados")
        void whenFindProductsByNameWithValidName_thenReturnsMatchingProducts() {
            // Arrange
            mockProductList = EntityModelFactory.defaultProductList();
            when(productRepository.findProductsByName("Pizza")).thenReturn(mockProductList);

            // Act
            List<ProductSummaryResponseDto> result = productService.findProductsByName("Pizza");

            // Assert
            assertNotNull(result, "Lista no debe ser null");
            assertEquals(2, result.size(), "Debe haber 2 productos");
            assertEquals(mockProduct.getName(), result.get(0).name(), "Nombre debe coincidir");

            // Verify
            verify(productRepository, times(1)).findProductsByName("Pizza");
        }

        /**
         * Test que verifica que nombre vacío lanza excepción.
         */
        @Test
        @DisplayName("findProductsByName() - Lanza IllegalArgumentException para nombre vacío")
        void whenFindProductsByNameWithEmptyName_thenThrowsIllegalArgumentException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> productService.findProductsByName(""));

            // Verify - No debe consultar repositorio
            verify(productRepository, never()).findProductsByName(any());
        }

        /**
         * Test que verifica que nombre con solo espacios lanza excepción.
         */
        @Test
        @DisplayName("findProductsByName() - Lanza IllegalArgumentException para nombre solo espacios")
        void whenFindProductsByNameWithOnlySpaces_thenThrowsIllegalArgumentException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> productService.findProductsByName("   "));

            // Verify
            verify(productRepository, never()).findProductsByName(any());
        }

        /**
         * Test que verifica que nombre muy corto (< 2 chars) lanza excepción.
         */
        @Test
        @DisplayName("findProductsByName() - Lanza IllegalArgumentException para nombre < 2 caracteres")
        void whenFindProductsByNameWithShortName_thenThrowsIllegalArgumentException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> productService.findProductsByName("P"));

            // Verify
            verify(productRepository, never()).findProductsByName(any());
        }

        /**
         * Test que verifica que nombre null lanza excepción.
         */
        @Test
        @DisplayName("findProductsByName() - Lanza IllegalArgumentException para nombre null")
        void whenFindProductsByNameWithNullName_thenThrowsIllegalArgumentException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> productService.findProductsByName(null));

            // Verify
            verify(productRepository, never()).findProductsByName(any());
        }

        /**
         * Test que verifica búsqueda con resultado vacío.
         */
        @Test
        @DisplayName("findProductsByName() - Retorna lista vacía si no hay coincidencias")
        void whenFindProductsByNameWithNoMatches_thenReturnsEmptyList() {
            // Arrange
            when(productRepository.findProductsByName("Sushi")).thenReturn(Collections.emptyList());

            // Act
            List<ProductSummaryResponseDto> result = productService.findProductsByName("Sushi");

            // Assert
            assertNotNull(result, "Lista no debe ser null");
            assertTrue(result.isEmpty(), "Lista debe estar vacía");

            // Verify
            verify(productRepository, times(1)).findProductsByName("Sushi");
        }
    }

    // ==================== findProductsByCategoryId() ====================

    @Nested
    @DisplayName("findProductsByCategoryId()")
    class FindProductsByCategoryIdTests {

        /**
         * Test que verifica búsqueda exitosa por categoría.
         */
        @Test
        @DisplayName("findProductsByCategoryId() - Retorna productos de categoría")
        void whenFindProductsByCategoryIdWithValidId_thenReturnsProducts() {
            // Arrange
            mockProductList = EntityModelFactory.defaultProductList();
            when(productRepository.findProductsByCategoryId(CATEGORY_ID)).thenReturn(mockProductList);

            // Act
            List<ProductSummaryResponseDto> result = productService.findProductsByCategoryId(CATEGORY_ID);

            // Assert
            assertNotNull(result, "Lista no debe ser null");
            assertEquals(2, result.size(), "Debe haber 1 producto");

            // Verify
            verify(productRepository, times(1)).findProductsByCategoryId(CATEGORY_ID);
        }

        /**
         * Test que verifica que categoría vacía retorna lista vacía.
         */
        @Test
        @DisplayName("findProductsByCategoryId() - Retorna lista vacía si categoría sin productos")
        void whenFindProductsByCategoryIdWithNoProducts_thenReturnsEmptyList() {
            // Arrange
            when(productRepository.findProductsByCategoryId(CATEGORY_ID)).thenReturn(Collections.emptyList());

            // Act
            List<ProductSummaryResponseDto> result = productService.findProductsByCategoryId(CATEGORY_ID);

            // Assert
            assertNotNull(result, "Lista no debe ser null");
            assertTrue(result.isEmpty(), "Lista debe estar vacía");

            // Verify
            verify(productRepository, times(1)).findProductsByCategoryId(CATEGORY_ID);
        }
    }

    // ==================== findProductsByRestaurantId() ====================

    @Nested
    @DisplayName("findProductsByRestaurantId()")
    class FindProductsByRestaurantIdTests {

        /**
         * Test que verifica búsqueda exitosa por restaurante.
         */
        @Test
        @DisplayName("findProductsByRestaurantId() - Retorna productos del restaurante")
        void whenFindProductsByRestaurantIdWithValidId_thenReturnsProducts() {
            // Arrange
            mockProductList = EntityModelFactory.defaultProductList();
            when(productRepository.findProductsByRestaurantId(RESTAURANT_ID)).thenReturn(mockProductList);

            // Act
            List<ProductResponseDto> result = productService.findProductsByRestaurantId(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Lista no debe ser null");
            assertEquals(2, result.size(), "Debe haber 2 producto2");

            // Verify
            verify(productRepository, times(1)).findProductsByRestaurantId(RESTAURANT_ID);
        }

        /**
         * Test que verifica que restaurante vacío retorna lista vacía.
         */
        @Test
        @DisplayName("findProductsByRestaurantId() - Retorna lista vacía si restaurante sin productos")
        void whenFindProductsByRestaurantIdWithNoProducts_thenReturnsEmptyList() {
            // Arrange
            when(productRepository.findProductsByRestaurantId(RESTAURANT_ID)).thenReturn(Collections.emptyList());

            // Act
            List<ProductResponseDto> result = productService.findProductsByRestaurantId(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Lista no debe ser null");
            assertTrue(result.isEmpty(), "Lista debe estar vacía");

            // Verify
            verify(productRepository, times(1)).findProductsByRestaurantId(RESTAURANT_ID);
        }
    }
}