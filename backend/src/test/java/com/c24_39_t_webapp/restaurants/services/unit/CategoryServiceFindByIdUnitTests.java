package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
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
import static org.mockito.Mockito.*;

/**
 * Test unitarios para CategoryService.findCategoryById()
 *
 * ✅ Categories vienen de EntityModelFactory
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Encuentra categoría por ID válido
 * ✅ Categoría no encontrada → Lanza excepción
 * ✅ ID inválido (null, <= 0) → Lanza excepción
 * ✅ Mapeo a CategoryResponseDto
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("CategoryService - findCategoryById()")
class CategoryServiceFindByIdUnitTests {

    private static final Long CATEGORY_ID = 1L;
    private static final String CATEGORY_NAME = "Pizzas";
    private static final String CATEGORY_DESCRIPTION = "Deliciosas pizzas";

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.CategoryRepository categoryRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.CategoryServiceImpl categoryService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - findCategoryById()")
    class SuccessCases {

        private Category existingCategory;

        @BeforeEach
        void setUp() {
            // ✅ Categoría existente desde factory
            existingCategory = EntityModelFactory.category(CATEGORY_ID, CATEGORY_NAME);
            existingCategory.setDescription(CATEGORY_DESCRIPTION);
        }

        /**
         * Test: Encuentra categoría por ID válido
         *
         * Verificación:
         * ✅ CategoryRepository.findById() se llamó
         * ✅ Retorna CategoryResponseDto con datos correctos
         * ✅ ID y name coinciden
         */
        @Test
        @DisplayName("Categoría existe → Retorna datos correctos")
        void whenCategoryExists_thenReturnsCategory() {
            // Arrange
            when(categoryRepository.findById(CATEGORY_ID))
                    .thenReturn(Optional.of(existingCategory));

            // Act
            CategoryResponseDto result = categoryService.findCategoryById(CATEGORY_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(CATEGORY_ID, result.ctg_id());
            assertEquals(CATEGORY_NAME, result.name());
            assertEquals(CATEGORY_DESCRIPTION, result.description());

            // Verify
            verify(categoryRepository, times(1)).findById(CATEGORY_ID);
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
        void whenMappingToDto_thenDataIsMappedCorrectly() {
            // Arrange
            when(categoryRepository.findById(CATEGORY_ID))
                    .thenReturn(Optional.of(existingCategory));

            // Act
            CategoryResponseDto result = categoryService.findCategoryById(CATEGORY_ID);

            // Assert
            assertEquals(existingCategory.getId(), result.ctg_id());
            assertEquals(existingCategory.getName(), result.name());
            assertEquals(existingCategory.getDescription(), result.description());

            // Verify
            verify(categoryRepository, times(1)).findById(CATEGORY_ID);
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - findCategoryById()")
    class ErrorCases {

        /**
         * Test: Categoría no encontrada
         *
         * Verificación:
         * ✅ Lanza CategoryNotFoundException
         * ✅ Mensaje contiene información sobre el ID
         */
        @Test
        @DisplayName("Categoría no encontrada → Lanza CategoryNotFoundException")
        void whenCategoryNotFound_thenThrowsCategoryNotFoundException() {
            // Arrange
            when(categoryRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            CategoryNotFoundException exception = assertThrows(
                    CategoryNotFoundException.class,
                    () -> categoryService.findCategoryById(999L),
                    "Debe lanzar CategoryNotFoundException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no se encontró"));

            // Verify
            verify(categoryRepository, times(1)).findById(999L);
        }

        /**
         * Test: ID null
         *
         * Verificación:
         * ✅ Lanza CategoryNotFoundException
         * ✅ No llama a findById()
         */
        @Test
        @DisplayName("ID null → Lanza CategoryNotFoundException")
        void whenIdIsNull_thenThrowsCategoryNotFoundException() {
            // Act & Assert
            CategoryNotFoundException exception = assertThrows(
                    CategoryNotFoundException.class,
                    () -> categoryService.findCategoryById(null),
                    "Debe lanzar CategoryNotFoundException por ID null"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no es válido"));

            // Verify - findById() NO debe llamarse
            verify(categoryRepository, never()).findById(any());
        }

        /**
         * Test: ID <= 0
         *
         * Verificación:
         * ✅ Lanza CategoryNotFoundException
         * ✅ No llama a findById()
         */
        @Test
        @DisplayName("ID <= 0 → Lanza CategoryNotFoundException")
        void whenIdIsNegativeOrZero_thenThrowsCategoryNotFoundException() {
            // Act & Assert
            CategoryNotFoundException exception = assertThrows(
                    CategoryNotFoundException.class,
                    () -> categoryService.findCategoryById(0L),
                    "Debe lanzar CategoryNotFoundException por ID <= 0"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no es válido"));

            // Verify - findById() NO debe llamarse
            verify(categoryRepository, never()).findById(any());
        }

        /**
         * Test: ID negativo
         *
         * Verificación:
         * ✅ Lanza CategoryNotFoundException
         */
        @Test
        @DisplayName("ID negativo → Lanza CategoryNotFoundException")
        void whenIdIsNegative_thenThrowsCategoryNotFoundException() {
            // Act & Assert
            CategoryNotFoundException exception = assertThrows(
                    CategoryNotFoundException.class,
                    () -> categoryService.findCategoryById(-1L),
                    "Debe lanzar CategoryNotFoundException por ID negativo"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no es válido"));

            // Verify
            verify(categoryRepository, never()).findById(any(Long.class));
        }
    }
}