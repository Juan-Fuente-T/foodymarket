package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.exception.ProductNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.models.Product;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit Tests para ProductServiceImpl.deleteProduct()
 *
 * Cobertura:
 * ✅ Éxito: Eliminar producto válido
 * ✅ Error: Producto no encontrado
 * ✅ Error: Sin permisos
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("ProductServiceImpl.deleteProduct() - Unit Tests")
class ProductServiceDeleteUnitTests {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private static final String OWNER_EMAIL = "owner@restaurant.com";
    private static final long PRODUCT_ID = 1L;
    private static final long RESTAURANT_ID = 1L;

    private Product mockProduct;
    private Restaurant mockRestaurant;
    private UserEntity mockOwner;

    @BeforeEach
    void setUp() {
        log.info("Setting up test data for deleteProduct tests");

        // Usuario propietario
        mockOwner = EntityModelFactory.userEntity(1L, OWNER_EMAIL);

        // Restaurante
        mockRestaurant = EntityModelFactory.restaurant(RESTAURANT_ID, mockOwner);

        // Producto
        mockProduct = EntityModelFactory.product(PRODUCT_ID, mockRestaurant, EntityModelFactory.defaultCategory());
    }

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases")
    class SuccessCases {

        /**
         * Test que verifica eliminación exitosa de producto.
         */
        @Test
        @DisplayName("deleteProduct() - Elimina producto exitosamente")
        void whenDeleteProductWithValidId_thenProductDeletedSuccessfully() {
            // Arrange
            setAuthentication(OWNER_EMAIL);
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));

            // Act
            productService.deleteProduct(PRODUCT_ID);

            // Assert & Verify
            verify(productRepository, times(1)).findById(PRODUCT_ID);
            verify(productRepository, times(1)).deleteById(PRODUCT_ID);
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
        @DisplayName("deleteProduct() - Lanza ProductNotFoundException si producto no existe")
        void whenDeleteProductNotFound_thenThrowsProductNotFoundException() {
            // Arrange
            setAuthentication(OWNER_EMAIL);
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ProductNotFoundException.class,
                    () -> productService.deleteProduct(PRODUCT_ID));

            // Verify - No debe eliminar
            verify(productRepository, never()).deleteById(PRODUCT_ID);
        }

        /**
         * Test que verifica que sin permisos lanza excepción.
         */
        @Test
        @DisplayName("deleteProduct() - Lanza UnauthorizedAccessException sin permisos")
        void whenDeleteProductWithoutPermissions_thenThrowsUnauthorizedAccessException() {
            // Arrange
            setAuthentication("other@restaurant.com");  // Otro usuario
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));

            // Act & Assert
            assertThrows(UnauthorizedAccessException.class,
                    () -> productService.deleteProduct(PRODUCT_ID));

            // Verify - No debe eliminar
            verify(productRepository, never()).deleteById(PRODUCT_ID);
        }

        /**
         * Test que verifica null pointer safety cuando UserEntity es null.
         */
        @Test
        @DisplayName("deleteProduct() - Lanza UnauthorizedAccessException cuando UserEntity es null")
        void whenDeleteProductWithNullUserEntity_thenThrowsUnauthorizedAccessException() {
            // Arrange
            setAuthentication(OWNER_EMAIL);
            mockRestaurant.setUserEntity(null);  // Forzar null
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(mockProduct));

            // Act & Assert
            assertThrows(UnauthorizedAccessException.class,
                    () -> productService.deleteProduct(PRODUCT_ID));

            // Verify
            verify(productRepository, never()).deleteById(PRODUCT_ID);
        }
    }

    // ==================== HELPER METHODS ====================

    private void setAuthentication(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(email, null));
        SecurityContextHolder.setContext(context);
    }
}