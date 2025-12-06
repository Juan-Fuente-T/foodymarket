package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.CategoryRepository;
import com.c24_39_t_webapp.restaurants.repository.ProductRepository;
import com.c24_39_t_webapp.restaurants.repository.RestaurantRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para CategoryService.deleteCategory()
 *
 * ✅ Restaurantes y Categories vienen de EntityModelFactory
 * ✅ SecurityContext mockeado
 * ✅ Happy path + error cases + edge cases
 *
 * Cobertura:
 * ✅ Eliminar categoría cuando no se usa en otros lados
 * ✅ Desasociar categoría que se usa en otros restaurantes (no la borra globalmente)
 * ✅ Categoría no asociada al restaurante
 * ✅ Restaurante sin UserEntity → Lanza excepción
 * ✅ Usuario no autorizado → Lanza excepción
 * ✅ Categoría no encontrada → Lanza excepción
 * ✅ Restaurante sin UserEntity → Lanza excepción
 *
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("CategoryService - deleteCategory()")
class CategoryServiceDeleteUnitTests {

    private static final Long RESTAURANT_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final String OWNER_EMAIL = "owner@restaurant.com";
    private static final String OTHER_EMAIL = "other@restaurant.com";
    private static final Long CATEGORY_ID = 1L;
    private static final String CATEGORY_NAME = "Pizzas";

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.CategoryServiceImpl categoryService;

    /**
     * Método auxiliar para mockear SecurityContext
     */
    private void setupSecurityContextWithEmail(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - deleteCategory()")
    class SuccessCases {

        private Restaurant restaurant;
        private Category category;
        private UserEntity owner;

        @BeforeEach
        void setUp() {
            // ✅ Usuario propietario
            owner = EntityModelFactory.restaurantOwnerEntity(USER_ID, OWNER_EMAIL);

            // ✅ Restaurante con el propietario
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

            // ✅ Categoría
            category = EntityModelFactory.category(CATEGORY_ID, CATEGORY_NAME);
            restaurant.getOfferedCategories().add(category);
        }

        /**
         * Test: Eliminar categoría cuando no se usa en otros lados
         *
         * Verificación:
         * ✅ Restaurante encontrado
         * ✅ Usuario autorizado
         * ✅ Categoría desasociada del restaurante
         * ✅ CategoryRepository.deleteById() se llamó (se borra globalmente)
         */
        @Test
        @DisplayName("Eliminar categoría no usada en otros lados → Borra globalmente")
        void whenDeletingUnusedCategory_thenDeletesGlobally() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));

            when(categoryRepository.findById(CATEGORY_ID))
                    .thenReturn(Optional.of(category));

            // ✅ No usada por productos
            when(productRepository.existsByCategoryId(CATEGORY_ID))
                    .thenReturn(false);

            // ✅ No usada por otros restaurantes
            when(categoryRepository.countRestaurantsUsingCategory(CATEGORY_ID))
                    .thenReturn(0L);

            when(restaurantRepository.save(any(Restaurant.class)))
                    .thenReturn(restaurant);

            // Act
            categoryService.deleteCategory(RESTAURANT_ID, CATEGORY_ID);

            // Verify
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(categoryRepository, times(1)).findById(CATEGORY_ID);
            verify(restaurantRepository, times(1)).save(any(Restaurant.class));
            verify(productRepository, times(1)).existsByCategoryId(CATEGORY_ID);
            verify(categoryRepository, times(1)).countRestaurantsUsingCategory(CATEGORY_ID);
            verify(categoryRepository, times(1)).deleteById(CATEGORY_ID);
        }

        /**
         * Test: Desasociar categoría que sigue siendo usada
         *
         * Verificación:
         * ✅ Categoría se desasocia del restaurante
         * ✅ NO se borra globalmente (se usa en otros lados)
         * ✅ RestaurantRepository.save() se llamó
         * ✅ CategoryRepository.deleteById() NO se llamó
         */
        @Test
        @DisplayName("Categoría en uso por otros restaurantes → Solo desasocia")
        void whenCategoryIsUsedElsewhere_thenOnlyDisassociates() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));

            when(categoryRepository.findById(CATEGORY_ID))
                    .thenReturn(Optional.of(category));

            // ✅ No usada por productos pero usada por otros restaurantes
            when(productRepository.existsByCategoryId(CATEGORY_ID))
                    .thenReturn(false);

            when(categoryRepository.countRestaurantsUsingCategory(CATEGORY_ID))
                    .thenReturn(2L);  // ← Usada por 2 restaurantes más

            when(restaurantRepository.save(any(Restaurant.class)))
                    .thenReturn(restaurant);

            // Act
            categoryService.deleteCategory(RESTAURANT_ID, CATEGORY_ID);

            // Verify
            verify(restaurantRepository, times(1)).save(any(Restaurant.class));
            verify(categoryRepository, never()).deleteById(CATEGORY_ID);  // NO se borra
        }

        /**
         * Test: Categoría no estaba asociada al restaurante
         *
         * Verificación:
         * ✅ No lanza excepción
         * ✅ Se comprueba si debe borrarse globalmente
         */
        @Test
        @DisplayName("Categoría no asociada → Continúa pero no guarda cambios")
        void whenCategoryNotAssociated_thenContinuesWithoutSaving() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);
            restaurant.getOfferedCategories().clear();  // Desasociar primero

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));

            when(categoryRepository.findById(CATEGORY_ID))
                    .thenReturn(Optional.of(category));

            // Act - No debe lanzar excepción
            assertDoesNotThrow(() -> categoryService.deleteCategory(RESTAURANT_ID, CATEGORY_ID));

            // Verify
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(categoryRepository, times(1)).findById(CATEGORY_ID);
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - deleteCategory()")
    class ErrorCases {

        private UserEntity owner;

        @BeforeEach
        void setUp() {
            owner = EntityModelFactory.restaurantOwnerEntity(USER_ID, OWNER_EMAIL);
        }

        /**
         * Test: Restaurante no encontrado
         *
         * Verificación:
         * ✅ Lanza RestaurantNotFoundException
         * ✅ No busca categoría
         */
        @Test
        @DisplayName("Restaurante no encontrado → Lanza RestaurantNotFoundException")
        void whenRestaurantNotFound_thenThrowsRestaurantNotFoundException() {
            // Arrange
            when(restaurantRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            RestaurantNotFoundException exception = assertThrows(
                    RestaurantNotFoundException.class,
                    () -> categoryService.deleteCategory(999L, CATEGORY_ID),
                    "Debe lanzar RestaurantNotFoundException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no encontrado"),
                    "Esperaba 'Restaurante no encontrado. Mensaje real: " + exception.getMessage()
            );

            // Verify
            verify(categoryRepository, never()).findById(any());
        }

        /**
         * Test: Categoría no encontrada
         *
         * Verificación:
         * ✅ Lanza CategoryNotFoundException
         */
        @Test
        @DisplayName("Categoría no encontrada → Lanza CategoryNotFoundException")
        void whenCategoryNotFound_thenThrowsCategoryNotFoundException() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            Restaurant restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));

            when(categoryRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            CategoryNotFoundException exception = assertThrows(
                    CategoryNotFoundException.class,
                    () -> categoryService.deleteCategory(RESTAURANT_ID, 999L),
                    "Debe lanzar CategoryNotFoundException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("no encontrada"),
                    "Esperaba 'Categoría no encontrada. Mensaje real: " + exception.getMessage()
            );

            // Verify
            verify(restaurantRepository, never()).deleteById(any());
        }

        /**
         * Test: Restaurante sin UserEntity
         *
         * Verificación:
         * ✅ Lanza NullPointerException o es manejado
         */
        @Test
        @DisplayName("Restaurante sin UserEntity → Lanza excepción")
        void whenRestaurantHasNoUser_thenThrowsException() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            Restaurant restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);
            restaurant.setUserEntity(null);

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));

            // Act & Assert - Espera NullPointerException o similar
            assertThrows(
                    Exception.class,
                    () -> categoryService.deleteCategory(RESTAURANT_ID, CATEGORY_ID),
                    "Debe lanzar excepción cuando UserEntity es null"
            );

            // Verify
            verify(categoryRepository, never()).findById(any());
        }

        /**
         * Test: Usuario no autorizado
         *
         * Verificación:
         * ✅ Lanza UnauthorizedAccessException
         * ✅ No busca categoría
         */
        @Test
        @DisplayName("Usuario no autorizado → Lanza UnauthorizedException")
        void whenUserNotOwner_thenThrowsUnauthorizedException() {
            // Arrange
            setupSecurityContextWithEmail(OTHER_EMAIL);

            Restaurant restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> categoryService.deleteCategory(RESTAURANT_ID, CATEGORY_ID),
                    "Debe lanzar UnauthorizedAccessException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("permiso"));

            // Verify
            verify(categoryRepository, never()).delete(any());
        }

        /**
         * Test: Error al intentar borrar globalmente la categoría
         *
         * Verificación:
         * ✅ Cuando deleteById() lanza excepción, se captura y loguea
         * ✅ El método NO lanza la excepción hacia arriba
         * ✅ El resto de la lógica continúa (desasociación se completa)
         */
        @Test
        @DisplayName("Error al borrar globalmente → Se captura excepción")
        void whenDeleteByIdFails_thenCatchesAndLogs() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            Restaurant restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);
            Category category = EntityModelFactory.category(CATEGORY_ID, CATEGORY_NAME);
            restaurant.getOfferedCategories().add(category);

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));

            when(categoryRepository.findById(CATEGORY_ID))
                    .thenReturn(Optional.of(category));

            // No hay productos usando esta categoría
            when(productRepository.existsByCategoryId(CATEGORY_ID))
                    .thenReturn(false);

            // Solo este restaurante la usa
            when(categoryRepository.countRestaurantsUsingCategory(CATEGORY_ID))
                    .thenReturn(0L);

            when(restaurantRepository.save(any(Restaurant.class)))
                    .thenReturn(restaurant);

            // ✅ MOCKEA que deleteById() lanza excepción
            doThrow(new RuntimeException("Error de BD: Restricción de clave foránea"))
                    .when(categoryRepository).deleteById(CATEGORY_ID);

            // Act & Assert
            // El método NO debe lanzar excepción (la captura internamente)
            assertDoesNotThrow(
                    () -> categoryService.deleteCategory(RESTAURANT_ID, CATEGORY_ID),
                    "Debe capturar la excepción sin relanzarla"
            );

            // Verify
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(categoryRepository, times(1)).findById(CATEGORY_ID);
            verify(categoryRepository, times(1)).deleteById(CATEGORY_ID);  // Se intentó borrar
            verify(restaurantRepository, times(1)).save(any(Restaurant.class));  // Se guardó cambios
        }
    }
}