package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para CategoryService.findAllCategories()
 *
 * ✅ Categories vienen de EntityModelFactory
 * ✅ Happy path + edge cases
 *
 * Cobertura:
 * ✅ Obtener lista de categorías
 * ✅ Mapeo a CategoryResponseDto
 * ✅ Lista vacía
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("CategoryService - findAllCategories()")
class CategoryServiceFindAllUnitTests {

    private static final Long CATEGORY_ID_1 = 1L;
    private static final String CATEGORY_NAME_1 = "Pizzas";
    private static final Long CATEGORY_ID_2 = 2L;
    private static final String CATEGORY_NAME_2 = "Pastas";
    private static final Long CATEGORY_ID_3 = 3L;
    private static final String CATEGORY_NAME_3 = "Ensaladas";

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.CategoryRepository categoryRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.CategoryServiceImpl categoryService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - findAllCategories()")
    class SuccessCases {

        private List<Category> categoryList;

        @BeforeEach
        void setUp() {
            // ✅ Crear lista de categorías usando factory
            Category category1 = EntityModelFactory.category(CATEGORY_ID_1, CATEGORY_NAME_1);
            category1.setDescription("Deliciosas pizzas");

            Category category2 = EntityModelFactory.category(CATEGORY_ID_2, CATEGORY_NAME_2);
            category2.setDescription("Pastas italianas");

            Category category3 = EntityModelFactory.category(CATEGORY_ID_3, CATEGORY_NAME_3);
            category3.setDescription("Ensaladas frescas");

            categoryList = Arrays.asList(category1, category2, category3);
        }

        /**
         * Test: Obtener lista de categorías
         *
         * Verificación:
         * ✅ CategoryRepository.findAll() se llamó
         * ✅ Retorna lista con datos correctos
         * ✅ Size correcto
         */
        @Test
        @DisplayName("Obtener lista de categorías → Retorna lista correcta")
        void whenGettingAllCategories_thenReturnsListSuccessfully() {
            // Arrange
            when(categoryRepository.findAll())
                    .thenReturn(categoryList);

            // Act
            List<CategoryResponseDto> result = categoryService.findAllCategories();

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(3, result.size(), "Debe retornar 3 categorías");
            assertEquals(CATEGORY_NAME_1, result.get(0).name());
            assertEquals(CATEGORY_NAME_2, result.get(1).name());
            assertEquals(CATEGORY_NAME_3, result.get(2).name());

            // Verify
            verify(categoryRepository, times(1)).findAll();
        }

        /**
         * Test: Mapeo correcto a CategoryResponseDto
         *
         * Verificación:
         * ✅ ID se mapea correctamente
         * ✅ Name se mapea correctamente
         * ✅ Description se mapea correctamente
         */
        @Test
        @DisplayName("Mapeo a CategoryResponseDto → Datos correctos")
        void whenMappingToDtoList_thenDataIsMappedCorrectly() {
            // Arrange
            when(categoryRepository.findAll())
                    .thenReturn(categoryList);

            // Act
            List<CategoryResponseDto> result = categoryService.findAllCategories();

            // Assert
            for (int i = 0; i < result.size(); i++) {
                CategoryResponseDto dto = result.get(i);
                Category entity = categoryList.get(i);

                assertEquals(entity.getId(), dto.ctg_id(), "ID debe coincidir");
                assertEquals(entity.getName(), dto.name(), "Name debe coincidir");
                assertEquals(entity.getDescription(), dto.description(), "Description debe coincidir");
            }

            // Verify
            verify(categoryRepository, times(1)).findAll();
        }

        /**
         * Test: Una única categoría en la lista
         *
         * Verificación:
         * ✅ Retorna lista con 1 elemento
         */
        @Test
        @DisplayName("Una única categoría → Retorna lista con 1 elemento")
        void whenOnlyOneCategory_thenReturnsListWithOneElement() {
            // Arrange
            Category singleCategory = EntityModelFactory.category(CATEGORY_ID_1, CATEGORY_NAME_1);
            List<Category> singleList = Collections.singletonList(singleCategory);

            when(categoryRepository.findAll())
                    .thenReturn(singleList);

            // Act
            List<CategoryResponseDto> result = categoryService.findAllCategories();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(CATEGORY_NAME_1, result.get(0).name());

            // Verify
            verify(categoryRepository, times(1)).findAll();
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases - findAllCategories()")
    class EdgeCases {

        /**
         * Test: Lista vacía de categorías
         *
         * Verificación:
         * ✅ Retorna Collections.emptyList()
         * ✅ No es null
         * ✅ Size es 0
         */
        @Test
        @DisplayName("Sin categorías → Retorna lista vacía")
        void whenNoCCategories_thenReturnsEmptyList() {
            // Arrange
            when(categoryRepository.findAll())
                    .thenReturn(new ArrayList<>());

            // Act
            List<CategoryResponseDto> result = categoryService.findAllCategories();

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertTrue(result.isEmpty(), "Lista debe estar vacía");
            assertEquals(0, result.size());

            // Verify
            verify(categoryRepository, times(1)).findAll();
        }

        /**
         * Test: Lista grande de categorías
         *
         * Verificación:
         * ✅ Retorna lista con muchos elementos
         * ✅ Todos se mapean correctamente
         */
        @Test
        @DisplayName("Muchas categorías → Retorna lista completa")
        void whenManyCategories_thenReturnsCompleteList() {
            // Arrange
            List<Category> largeList = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Category category = EntityModelFactory.category(
                        (long) i,
                        "Category" + i
                );
                category.setDescription("Description " + i);
                largeList.add(category);
            }

            when(categoryRepository.findAll())
                    .thenReturn(largeList);

            // Act
            List<CategoryResponseDto> result = categoryService.findAllCategories();

            // Assert
            assertNotNull(result);
            assertEquals(10, result.size());
            assertEquals("Category1", result.get(0).name());
            assertEquals("Category10", result.get(9).name());

            // Verify
            verify(categoryRepository, times(1)).findAll();
        }
    }
}