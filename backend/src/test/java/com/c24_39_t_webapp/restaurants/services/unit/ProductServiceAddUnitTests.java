package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.ProductRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.factories.ProductFactory;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Product;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.CategoryRepository;
import com.c24_39_t_webapp.restaurants.repository.ProductRepository;
import com.c24_39_t_webapp.restaurants.repository.RestaurantRepository;
import com.c24_39_t_webapp.restaurants.services.impl.ProductServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * Unit Tests para ProductServiceImpl.addProduct()
 * Arquitectura:
 * - @ExtendWith(MockitoExtension.class): Mockito sin Spring
 * - @Mock: Repositories mockeados
 * - @InjectMocks: ProductServiceImpl inyecta los mocks
 * - Sin @SpringBootTest, sin BD real
 * - Testea SOLO la lógica de addProduct()
 *
 * Patrón AAA: Arrange, Act, Assert
 * Cobertura:
 * ✅ Éxito: Crear producto válido
 * ✅ Error: Restaurante no encontrado
 * ✅ Error: Categoría no encontrada
 * ✅ Error: Sin permisos
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("ProductServiceImpl.addProduct() - Unit Tests")
class ProductServiceAddUnitTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private static final String OWNER_EMAIL = "owner@restaurant.com";
    private static final long RESTAURANT_ID = 1L;
    private static final long CATEGORY_ID = 1L;
    private static final long PRODUCT_ID = 1L;

    private Restaurant mockRestaurant;
    private Category mockCategory;
    private UserEntity mockOwner;

    @BeforeEach
    void setUp() {
        log.info("Setting up test data for addProduct tests");

        // Usa EntityModelFactory para crear entidades
        mockOwner = EntityModelFactory.userEntity(1L, OWNER_EMAIL);
        mockRestaurant = EntityModelFactory.restaurant(RESTAURANT_ID, mockOwner);
        mockCategory = EntityModelFactory.defaultCategory();
    }

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases")
    class SuccessCases {

        /**
         * Test que verifica que al crear un producto con datos válidos,
         * el servicio guarda correctamente y retorna ProductResponseDto.
         *
         * Arrange: Mock del restaurant y category, ProductRequestDto válido
         * Act: Llamar a addProduct()
         * Assert: Verificar retorno y que se guardó el producto
         */
        @Test
        @DisplayName("addProduct() - Crea producto exitosamente con datos válidos")
        void whenAddProductWithValidData_thenProductCreatedSuccessfully() {
            // Arrange
            ProductRequestDto dto = ProductFactory.defaultProductRequest(RESTAURANT_ID);
            setAuthentication(OWNER_EMAIL);

            when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(mockRestaurant));
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(mockCategory));

            Product savedProduct = EntityModelFactory.product(PRODUCT_ID, mockRestaurant, mockCategory);

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // Act
            ProductResponseDto result = productService.addProduct(dto);

            // Assert
            assertNotNull(result);
            assertEquals(PRODUCT_ID, result.prd_id());

            // Captura el producto que se guarda
            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository, times(1)).save(productCaptor.capture());

            Product capturedProduct = productCaptor.getValue();
            assertEquals(dto.name(), capturedProduct.getName(), "Nombre debe coincidir");
            assertEquals(dto.description(), capturedProduct.getDescription(), "Descripción debe coincidir");
            assertEquals(mockRestaurant, capturedProduct.getRestaurant(), "Restaurant debe asignarse");
            assertEquals(mockCategory, capturedProduct.getCategory(), "Categoría debe asignarse");

            // Verify
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(categoryRepository, times(1)).findById(CATEGORY_ID);
            verify(productRepository, times(1)).save(any(Product.class));
        }

        /**
         * Test que verifica que isActive por defecto es true si no viene en el DTO.
         */
        @Test
        @DisplayName("addProduct() - Asigna isActive=true por defecto")
        void whenAddProductWithoutIsActive_thenDefaultsToTrue() {
            // Arrange
            ProductRequestDto dto = new ProductRequestDto(
                    RESTAURANT_ID,
                    CATEGORY_ID,
                    "Test Product",
                    "Test Description",
                    new BigDecimal("10.00"),
                    "image.jpg",
                    null,  // isActive es null
                    10
            );
            setAuthentication(OWNER_EMAIL);

            when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(mockRestaurant));
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(mockCategory));

            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            Product savedProduct = new Product();
            savedProduct.setPrd_id(PRODUCT_ID);
            savedProduct.setRestaurant(mockRestaurant);
            savedProduct.setCategory(mockCategory);
            savedProduct.setName(dto.name());
            savedProduct.setIsActive(true);  // Debe ser true

            when(productRepository.save(productCaptor.capture())).thenReturn(savedProduct);

            // Act
            productService.addProduct(dto);

            // Assert
            Product capturedProduct = productCaptor.getValue();
            assertTrue(capturedProduct.getIsActive(), "isActive debe ser true por defecto");
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases")
    class ErrorCases {

        /**
         * Test que verifica que si el restaurante no existe,
         * lanza RestaurantNotFoundException.
         */
        @Test
        @DisplayName("addProduct() - Lanza RestaurantNotFoundException si restaurante no existe")
        void whenAddProductWithInvalidRestaurant_thenThrowsRestaurantNotFoundException() {
            // Arrange
            ProductRequestDto dto = ProductFactory.defaultProductRequest(RESTAURANT_ID);
            setAuthentication(OWNER_EMAIL);

            when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RestaurantNotFoundException.class,
                    () -> productService.addProduct(dto),
                    "Debe lanzar RestaurantNotFoundException");

            // Verify
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(categoryRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
        }

        /**
         * Test que verifica que si la categoría no existe,
         * lanza CategoryNotFoundException.
         */
        @Test
        @DisplayName("addProduct() - Lanza CategoryNotFoundException si categoría no existe")
        void whenAddProductWithInvalidCategory_thenThrowsCategoryNotFoundException() {
            // Arrange
            ProductRequestDto dto = ProductFactory.defaultProductRequest(RESTAURANT_ID);
            setAuthentication(OWNER_EMAIL);

            when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(mockRestaurant));
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(CategoryNotFoundException.class,
                    () -> productService.addProduct(dto),
                    "Debe lanzar CategoryNotFoundException");

            // Verify
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(categoryRepository, times(1)).findById(CATEGORY_ID);
            verify(productRepository, never()).save(any());
        }

        /**
         * Test que verifica que sin permisos (email distinto),
         * lanza UnauthorizedAccessException.
         */
        @Test
        @DisplayName("addProduct() - Lanza UnauthorizedAccessException sin permisos")
        void whenAddProductWithoutPermissions_thenThrowsUnauthorizedAccessException() {
            // Arrange
            ProductRequestDto dto = ProductFactory.defaultProductRequest(RESTAURANT_ID);
            setAuthentication("other@restaurant.com");  // Email diferente

            when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(mockRestaurant));
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(mockCategory));

            // Act & Assert
            assertThrows(UnauthorizedAccessException.class,
                    () -> productService.addProduct(dto),
                    "Debe lanzar UnauthorizedAccessException");

            // Verify
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(categoryRepository, times(1)).findById(CATEGORY_ID);
            verify(productRepository, never()).save(any());
        }

        /**
         * Test que verifica que el orden de validación es:
         * 1. Restaurante
         * 2. Categoría
         * 3. Permisos
         */
        @Test
        @DisplayName("addProduct() - Valida restaurante ANTES que categoría")
        void whenRestaurantNotFoundBeforeCategory_thenThrowsRestaurantNotFound() {
            // Arrange
            ProductRequestDto dto = ProductFactory.defaultProductRequest(RESTAURANT_ID);
            setAuthentication(OWNER_EMAIL);

            when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.empty());
            // No configurar categoryRepository, no debe llamarse

            // Act & Assert
            assertThrows(RestaurantNotFoundException.class,
                    () -> productService.addProduct(dto));

            // Verify que categoryRepository NUNCA se llamó
            verify(categoryRepository, never()).findById(any());
        }
    }

    // ==================== HELPER METHODS ====================

    private void setAuthentication(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(email, null));
        SecurityContextHolder.setContext(context);
    }
}