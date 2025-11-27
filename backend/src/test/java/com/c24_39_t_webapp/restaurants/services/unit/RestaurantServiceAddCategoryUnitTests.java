package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.CategoryRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantService - addCategoryToRestaurant()")
class RestaurantServiceAddCategoryUnitTests {

    private static final Long RESTAURANT_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final String OWNER_EMAIL = "owner@restaurant.com";
    private static final Long CATEGORY_ID = 1L;
    private static final String CATEGORY_NAME = "Pizzas";

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.RestaurantRepository restaurantRepository;

    @Mock
    private com.c24_39_t_webapp.restaurants.services.ICategoryService categoryService;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.RestaurantServiceImpl restaurantService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - addCategoryToRestaurant()")
    class SuccessCases {

        private Restaurant restaurant;
        private Category category;
        private CategoryRequestDto categoryRequest;

        @BeforeEach
        void setUp() {
            // ✅ Crear restaurante
            UserEntity owner = EntityModelFactory.restaurantOwnerEntity(USER_ID, OWNER_EMAIL);
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

            // ✅ Crear categoría
            category = new Category();
            category.setId(CATEGORY_ID);
            category.setName(CATEGORY_NAME);
            category.setDescription("Deliciosas pizzas");

            // ✅ Request
            categoryRequest = new CategoryRequestDto(CATEGORY_NAME, "Deliciosas pizzas");
        }

        /**
         * Test: Agregar categoría nueva al restaurante
         *
         * Verificación:
         * ✅ Categoría se agrega correctamente
         * ✅ Restaurant.save() se llamó (porque es nueva)
         * ✅ Retorna CategoryResponseDto correcto
         */
        @Test
        @DisplayName("Agregar categoría nueva → Se agrega y guarda")
        void whenAddingNewCategory_thenCategoryIsAdded() {
            // Arrange
            when(categoryService.findOrCreateCategory(categoryRequest))
                    .thenReturn(category);

            when(restaurantRepository.findByIdFetchingCategories(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));

            when(restaurantRepository.save(any(Restaurant.class)))
                    .thenReturn(restaurant);

            // Act
            CategoryResponseDto result = restaurantService.addCategoryToRestaurant(
                    RESTAURANT_ID, categoryRequest
            );

            // Assert
            assertNotNull(result);
            assertEquals(CATEGORY_ID, result.ctg_id());
            assertEquals(CATEGORY_NAME, result.name());

            // Verify
            verify(categoryService, times(1)).findOrCreateCategory(categoryRequest);
            verify(restaurantRepository, times(1)).findByIdFetchingCategories(RESTAURANT_ID);
            verify(restaurantRepository, times(1)).save(any(Restaurant.class));
        }

        /**
         * Test: Agregar categoría que ya existe en el restaurante
         *
         * Verificación:
         * ✅ Categoría no se agrega (ya existe)
         * ✅ Restaurant.save() NO se llamó
         * ✅ Retorna CategoryResponseDto (pero sin guardar)
         */
        @Test
        @DisplayName("Categoría ya existe → No se guarda de nuevo")
        void whenCategoryAlreadyExists_thenNotSavedAgain() {
            // Arrange
            // Pre-agregar la categoría al restaurante
            restaurant.getOfferedCategories().add(category);

            when(categoryService.findOrCreateCategory(categoryRequest))
                    .thenReturn(category);

            when(restaurantRepository.findByIdFetchingCategories(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));

            // Act
            CategoryResponseDto result = restaurantService.addCategoryToRestaurant(
                    RESTAURANT_ID, categoryRequest
            );

            // Assert
            assertNotNull(result);
            assertEquals(CATEGORY_NAME, result.name());

            // Verify - save() NO se llamó porque la categoría ya estaba
            verify(restaurantRepository, never()).save(any(Restaurant.class));
        }

        /**
         * Test: Agregar múltiples categorías al mismo restaurante
         */
        @Test
        @DisplayName("Agregar múltiples categorías → Todas se agregan")
        void whenAddingMultipleCategories_thenAllAreAdded() {
            // Arrange
            Category category1 = new Category();
            category1.setId(1L);
            category1.setName("Pizzas");

            Category category2 = new Category();
            category2.setId(2L);
            category2.setName("Pastas");

            CategoryRequestDto request1 = new CategoryRequestDto("Pizzas", "Desc");
            CategoryRequestDto request2 = new CategoryRequestDto("Pastas", "Desc");

            // Primera categoría
            when(categoryService.findOrCreateCategory(request1))
                    .thenReturn(category1);
            when(restaurantRepository.findByIdFetchingCategories(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));
            when(restaurantRepository.save(any(Restaurant.class)))
                    .thenReturn(restaurant);

            // Act - Primera
            CategoryResponseDto result1 = restaurantService.addCategoryToRestaurant(
                    RESTAURANT_ID, request1
            );

            // Reset mock para segunda llamada
            reset(restaurantRepository);

            // Segunda categoría
            when(categoryService.findOrCreateCategory(request2))
                    .thenReturn(category2);
            when(restaurantRepository.findByIdFetchingCategories(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));
            when(restaurantRepository.save(any(Restaurant.class)))
                    .thenReturn(restaurant);

            CategoryResponseDto result2 = restaurantService.addCategoryToRestaurant(
                    RESTAURANT_ID, request2
            );

            // Assert
            assertNotNull(result1);
            assertNotNull(result2);
            assertEquals("Pizzas", result1.name());
            assertEquals("Pastas", result2.name());
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - addCategoryToRestaurant()")
    class ErrorCases {

        private CategoryRequestDto categoryRequest;

        @BeforeEach
        void setUp() {
            categoryRequest = new CategoryRequestDto(CATEGORY_NAME, "Deliciosas pizzas");
        }

        /**
         * Test: Restaurante no encontrado
         *
         * Verificación:
         * ✅ Lanza RestaurantNotFoundException
         * ✅ No intenta guardar
         */
        @Test
        @DisplayName("Restaurante no encontrado → Lanza excepción")
        void whenRestaurantNotFound_thenThrowsException() {
            // Arrange
            Category category = new Category();
            category.setId(CATEGORY_ID);
            category.setName(CATEGORY_NAME);

            when(categoryService.findOrCreateCategory(categoryRequest))
                    .thenReturn(category);

            when(restaurantRepository.findByIdFetchingCategories(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            RestaurantNotFoundException exception = assertThrows(
                    RestaurantNotFoundException.class,
                    () -> restaurantService.addCategoryToRestaurant(999L, categoryRequest)
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no encontrado"));

            // Verify
            verify(restaurantRepository, never()).save(any(Restaurant.class));
        }
    }
}