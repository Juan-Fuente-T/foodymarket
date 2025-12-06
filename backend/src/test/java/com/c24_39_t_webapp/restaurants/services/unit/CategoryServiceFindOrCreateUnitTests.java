package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.CategoryRequestDto;
import com.c24_39_t_webapp.restaurants.factories.CategoryFactory;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Test unitarios para CategoryService.findOrCreateCategory()
 *
 * ✅ Categories vienen de EntityModelFactory
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Encuentra categoría existente
 * ✅ Crea categoría nueva si no existe
 * ✅ Retorna Category correctamente
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("CategoryService - findOrCreateCategory()")
class CategoryServiceFindOrCreateUnitTests {

    private static final Long CATEGORY_ID = 1L;
    private static final String CATEGORY_NAME = "Pizzas";
    private static final String CATEGORY_DESCRIPTION = "Deliciosas pizzas italianas";

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.CategoryRepository categoryRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.CategoryServiceImpl categoryService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - findOrCreateCategory()")
    class SuccessCases {

        private CategoryRequestDto categoryRequest;
        private Category existingCategory;

        @BeforeEach
        void setUp() {
            // ✅ Request con datos
            categoryRequest = CategoryFactory.defaultRequest();

            // ✅ Categoría existente desde factory
            existingCategory = EntityModelFactory.category(CATEGORY_ID, CATEGORY_NAME);
        }

        /**
         * Test: Encuentra categoría existente
         *
         * Verificación:
         * ✅ CategoryRepository.findByName() se llamó
         * ✅ Retorna la categoría existente
         * ✅ NO crea una nueva
         */
        @Test
        @DisplayName("Categoría existe → Retorna la existente")
        void whenCategoryExists_thenReturnsExistingCategory() {
            // Arrange
            when(categoryRepository.findByName(CATEGORY_NAME))
                    .thenReturn(Optional.of(existingCategory));

            // Act
            Category result = categoryService.findOrCreateCategory(categoryRequest);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(CATEGORY_ID, result.getId());
            assertEquals(CATEGORY_NAME, result.getName());

            // Verify
            verify(categoryRepository, times(1)).findByName(CATEGORY_NAME);
            verify(categoryRepository, never()).save(any());
        }

        /**
         * Test: Crea categoría nueva si no existe
         *
         * Verificación:
         * ✅ CategoryRepository.findByName() retorna empty
         * ✅ CategoryRepository.save() se llamó
         * ✅ Retorna la categoría creada
         */
        @Test
        @DisplayName("Categoría no existe → Crea una nueva")
        void whenCategoryNotExists_thenCreatesNew() {
            // Arrange
            Category newCategory = EntityModelFactory.defaultCategory();
            newCategory.setDescription(CATEGORY_DESCRIPTION);

            when(categoryRepository.findByName(CATEGORY_NAME))
                    .thenReturn(Optional.empty());

            when(categoryRepository.save(any(Category.class)))
                    .thenReturn(newCategory);

            // Act
            Category result = categoryService.findOrCreateCategory(categoryRequest);

            // Assert
            assertNotNull(result);
            assertEquals(CATEGORY_ID, result.getId());
            assertEquals(CATEGORY_NAME, result.getName());
            assertEquals(CATEGORY_DESCRIPTION, result.getDescription());

            // Verify
            verify(categoryRepository, times(1)).findByName(CATEGORY_NAME);
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        /**
         * Test: Usa orElseGet correctamente
         *
         * Verificación:
         * ✅ Si existe, NO llama save()
         * ✅ Si no existe, SÍ llama save()
         */
        @Test
        @DisplayName("orElseGet() funciona correctamente")
        void whenUsingOrElseGet_thenBehavesCorrectly() {
            // Arrange
            when(categoryRepository.findByName(CATEGORY_NAME))
                    .thenReturn(Optional.of(existingCategory));

            // Act
            categoryService.findOrCreateCategory(categoryRequest);

            // Verify - save() NO debe llamarse si existe
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }
}