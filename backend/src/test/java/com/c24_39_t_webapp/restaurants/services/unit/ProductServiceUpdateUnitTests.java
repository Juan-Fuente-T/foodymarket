package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.ProductUpdateDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.ProductNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.factories.ProductFactory;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Product;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.CategoryRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para ProductServiceImpl.updateProduct()
 * <p>
 * Cobertura:
 * ✅ Éxito: Actualizar producto válido
 * ✅ Error: Producto no encontrado
 * ✅ Error: Categoría no encontrada
 * ✅ Error: Sin permisos
 * ✅ Verifica que solo los campos no-null se actualizan
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("ProductServiceImpl.updateProduct() - Unit Tests")
class ProductServiceUpdateUnitTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private static final String OWNER_EMAIL = "owner@restaurant.com";
    private static final long PRODUCT_ID = 1L;
    private static final long RESTAURANT_ID = 1L;
    private static final long CATEGORY_ID = 1L;
    private static final long OTHER_USER_ID = 2L;

    private Product mockProduct;
    private Category mockCategory;
    private Restaurant mockRestaurant;
    private UserEntity mockOwner;
    private UserEntity mockOtherUser;

    @BeforeEach
    void setUp() {
        log.info("Setting up test data for updateProduct tests");

        // Usuario propietario
        mockOwner = EntityModelFactory.userEntity(1L, OWNER_EMAIL);

        // Otro usuario
        mockOtherUser = EntityModelFactory.userEntity(OTHER_USER_ID, "other@restaurant.com");

        // Restaurante del propietario
        mockRestaurant = EntityModelFactory.restaurant(RESTAURANT_ID, mockOwner);

        // Categoría
        mockCategory = EntityModelFactory.pastasCategory();
//        mockCategory = EntityModelFactory.defaultCategory();

        // Producto
        mockProduct = EntityModelFactory.product(PRODUCT_ID, mockRestaurant, mockCategory);
    }

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases")
    class SuccessCases {

        /**
         * Test que verifica actualización exitosa de producto.
         */
        @Test
        @DisplayName("updateProduct() - Actualiza producto exitosamente")
        void whenUpdateProductWithValidData_thenProductUpdatedSuccessfully() {
            // Arrange
            ProductUpdateDto updateDto = ProductFactory.defaultUpdatedProduct(
                    RESTAURANT_ID,
                    "New Name",
                    "New Description"
            );
            setAuthentication(OWNER_EMAIL);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(mockCategory));
            when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

            // Act
            ProductResponseDto result = productService.updateProduct(PRODUCT_ID, updateDto);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(PRODUCT_ID, result.prd_id(), "ID debe coincidir");

            // Verify
            verify(productRepository, times(1)).findById(PRODUCT_ID);
            verify(categoryRepository, times(1)).findById(CATEGORY_ID);
            verify(productRepository, times(1)).save(any(Product.class));
        }

        /**
         * Test que verifica que solo campos no-null se actualizan.
         */
        @Test
        @DisplayName("updateProduct() - Actualiza solo campos no-null")
        void whenUpdateProductWithPartialData_thenOnlyUpdatesNonNullFields() {
            // Arrange
            ProductUpdateDto updateDto = ProductFactory.defaultUpdatedProductWith(
                    RESTAURANT_ID,
                    CATEGORY_ID,
                    "New Name",
                    null, // Es null, no debe actualizar
                    null,
                    null,
                    null,
                    null,
                    "Postres"
            );
            setAuthentication(OWNER_EMAIL);

            String originalDescription = mockProduct.getDescription();
            BigDecimal originalPrice = mockProduct.getPrice();

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(mockCategory));
            when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

            // Act
            productService.updateProduct(PRODUCT_ID, updateDto);

            // Assert
            assertEquals("New Name", mockProduct.getName(), "Name debe actualizarse");
            assertEquals(originalDescription, mockProduct.getDescription(), "Description no debe cambiar");
            assertEquals(originalPrice, mockProduct.getPrice(), "Price no debe cambiar");
        }

        /**
         * Test que verifica cambio de categoría.
         */
        @Test
        @DisplayName("updateProduct() - Cambia categoría correctamente")
        void whenUpdateProductChangeCategory_thenCategoryUpdated() {
            // Arrange
            Category newCategory = EntityModelFactory.pastasCategory();

            ProductUpdateDto updateDto = ProductFactory.defaultUpdatedProductWith(
                    null,
                    2L,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Pastas"
            );
            setAuthentication(OWNER_EMAIL);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
            when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

            // Act
            productService.updateProduct(PRODUCT_ID, updateDto);

            // Assert
            assertEquals(newCategory, mockProduct.getCategory(), "Categoría debe cambiar");

            // Verify
            verify(categoryRepository, times(1)).findById(2L);
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases")
    class ErrorCases {

        /**
         * Test que verifica que si el producto no existe, lanza excepción.
         */
        @Test
        @DisplayName("updateProduct() - Lanza ProductNotFoundException si producto no existe")
        void whenUpdateProductNotFound_thenThrowsProductNotFoundException() {
            // Arrange
            ProductUpdateDto updateDto = ProductFactory.defaultUpdatedProduct(RESTAURANT_ID, "New", "New");
            setAuthentication(OWNER_EMAIL);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProductNotFoundException.class,
                    () -> productService.updateProduct(PRODUCT_ID, updateDto));

            // Verify
            verify(categoryRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
        }

        /**
         * Test que verifica que sin permisos lanza excepción.
         */
        @Test
        @DisplayName("updateProduct() - Lanza UnauthorizedAccessException sin permisos")
        void whenUpdateProductWithoutPermissions_thenThrowsUnauthorizedAccessException() {
            // Arrange
            ProductUpdateDto updateDto = ProductFactory.defaultUpdatedProduct(RESTAURANT_ID, "New", "New");
            setAuthentication("other@restaurant.com");  // Otro usuario

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));

            // Act & Assert
            assertThrows(UnauthorizedAccessException.class,
                    () -> productService.updateProduct(PRODUCT_ID, updateDto));

            // Verify - No debe guardar
            verify(productRepository, never()).save(any());
        }

        /**
         * Test que verifica que si categoría nueva no existe, lanza excepción.
         */
        @Test
        @DisplayName("updateProduct() - Lanza CategoryNotFoundException si nueva categoría no existe")
        void whenUpdateProductWithInvalidCategory_thenThrowsCategoryNotFoundException() {
            // Arrange
            ProductUpdateDto updateDto = ProductFactory.defaultUpdatedProductWith(
                    null,
                    999L,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Unknown"
            );
            setAuthentication(OWNER_EMAIL);

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CategoryNotFoundException.class,
                    () -> productService.updateProduct(PRODUCT_ID, updateDto));

            // Verify
            verify(productRepository, never()).save(any());
        }

        /**
         * Test que verifica que se valida permisos ANTES que categoría.
         */
        @Test
        @DisplayName("updateProduct() - Valida permisos ANTES que categoría")
        void whenUpdateProductValidatesPermissionsBeforeCategory() {
            // Arrange
            ProductUpdateDto updateDto = ProductFactory.defaultUpdatedProduct(RESTAURANT_ID, "New", "New");
            setAuthentication("other@restaurant.com");

            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));
            // No configurar categoryRepository, no debe llamarse

            // Act & Assert
            assertThrows(UnauthorizedAccessException.class,
                    () -> productService.updateProduct(PRODUCT_ID, updateDto));

            // Verify
            verify(categoryRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
        }
    }

    // ==================== HELPER METHODS ====================

    private void setAuthentication(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(email, null));
        SecurityContextHolder.setContext(context);
    }
}