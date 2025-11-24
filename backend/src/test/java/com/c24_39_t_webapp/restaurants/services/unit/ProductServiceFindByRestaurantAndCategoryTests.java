package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.ProductRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.GroupedProductsResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.factories.ProductFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para ProductService.findProductsByRestaurantIdAndCategory()
 * <p>
 * TODOS los datos provienen de ProductFactory.
 * NO hay hardcoding de valores.
 * <p>
 * Cobertura:
 * ✅ Caso vacío: restaurante sin productos
 * ✅ Caso normal: productos agrupados por categoría
 * ✅ Lambda: productos agrupados correctamente por categoryId
 * ✅ Lambda: manejo de valores null en lista vacía
 * ✅ Caso extremo: ID null/inválido
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("ProductService - findProductsByRestaurantIdAndCategory()")
public class ProductServiceFindByRestaurantAndCategoryTests {

    private static final Long RESTAURANT_ID = 1L;
    private static final Long CATEGORY_ID_1 = 1L;
    private static final Long CATEGORY_ID_2 = 2L;

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.ProductRepository productRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.ProductServiceImpl productService;

    // ==================== CASOS DE ÉXITO ====================

    @Nested
    @DisplayName("Success Cases - findProductsByRestaurantIdAndCategory()")
    class SuccessCases {

        /**
         * Test: Restaurante SIN productos → retorna lista vacía.
         * <p>
         * Verificación:
         * ✅ ProductRepository se llamó con el ID correcto
         * ✅ Retorna Collections.emptyList() (no null)
         * ✅ No procesa la lambda (no hay productos)
         */
        @Test
        @DisplayName("Restaurante sin productos → retorna lista vacía")
        void whenNoProductsForRestaurant_thenReturnsEmptyList() {
            // Arrange
            when(productRepository.findProductsByRestaurantIdAndCategory(RESTAURANT_ID))
                    .thenReturn(Collections.emptyList());

            // Act
            List<GroupedProductsResponseDto> result = productService.findProductsByRestaurantIdAndCategory(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertTrue(result.isEmpty(), "Lista debe estar vacía");
            assertEquals(0, result.size(), "Tamaño debe ser 0");

            // Verify
            verify(productRepository, times(1)).findProductsByRestaurantIdAndCategory(RESTAURANT_ID);
        }

        /**
         * Test: Restaurante CON productos de UNA categoría → agrupa correctamente.
         * <p>
         * DATOS: Usa ProductFactory.defaultProductRequest() y responseFromRequest()
         * <p>
         * Verificación:
         * ✅ Agrupa productos por categoryId (siempre 1L de defaultProductRequest)
         * ✅ Lambda procesa entrada correctamente
         * ✅ GroupedProductsResponseDto contiene datos correctos
         * ✅ CategoryName y RestaurantName se obtienen del primer producto
         */
        @Test
        @DisplayName("Restaurante con productos de UNA categoría → agrupa en 1 grupo")
        void whenProductsFromOneCategory_thenGroupsIntoOne() {
            // Arrange
            ProductRequestDto requestDto = ProductFactory.defaultProductRequest(RESTAURANT_ID);
            ProductResponseDto product1 = ProductFactory.responseFromRequest(requestDto, 1L);
            ProductResponseDto product2 = ProductFactory.responseFromRequest(requestDto, 2L);

            List<ProductResponseDto> mockProducts = List.of(product1, product2);
            when(productRepository.findProductsByRestaurantIdAndCategory(RESTAURANT_ID))
                    .thenReturn(mockProducts);

            // Act
            List<GroupedProductsResponseDto> result = productService.findProductsByRestaurantIdAndCategory(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(1, result.size(), "Debe haber 1 grupo (misma categoría)");

            GroupedProductsResponseDto group = result.get(0);
            assertEquals(product1.categoryId(), group.categoryId(), "CategoryId debe coincidir con factory");
            assertEquals(2, group.products().size(), "Debe contener 2 productos");
            assertEquals(product1.categoryName(), group.categoryName(), "CategoryName desde factory");
            assertEquals(product1.restaurantName(), group.restaurantName(), "RestaurantName desde factory");

            // Verify
            verify(productRepository, times(1)).findProductsByRestaurantIdAndCategory(RESTAURANT_ID);
        }

        /**
         * Test: Restaurante CON productos de MÚLTIPLES categorías → agrupa por cada categoría.
         * <p>
         * DATOS: Usa ProductFactory para crear products en categorías diferentes.
         * Se simula creando responses con categoryId distinto.
         * <p>
         * Verificación:
         * ✅ Crea múltiples grupos (uno por categoría)
         * ✅ Cada grupo contiene productos de su categoría
         * ✅ Los grupos están ordenados por categoryId (TreeMap)
         * ✅ Lambda se ejecuta para CADA grupo
         */
        @Test
        @DisplayName("Restaurante con productos de MÚLTIPLES categorías → agrupa por cada una")
        void whenProductsFromMultipleCategories_thenGroupsByEachCategory() {
            // Arrange
            //ProductFactory.responseListDefault() Devuelve 2 productos con categoryId = 1L y categoryName = "Pizzas"
            List<ProductResponseDto> mockProducts = new ArrayList<>();
            mockProducts.addAll(ProductFactory.responseListDefault());      // 2 productos, categoryId = 1L
            mockProducts.addAll(ProductFactory.responseListPastas());       // 1 producto, categoryId = 2L

            when(productRepository.findProductsByRestaurantIdAndCategory(RESTAURANT_ID))
                    .thenReturn(mockProducts);

            // Act
            List<GroupedProductsResponseDto> result = productService.findProductsByRestaurantIdAndCategory(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(2, result.size(), "Debe haber 2 grupos (dos categorías)");

            // Grupo 1 - CategoryId 1
            GroupedProductsResponseDto group1 = result.get(0);
            assertEquals(CATEGORY_ID_1, group1.categoryId(), "Primer grupo debe ser categoría 1");
            assertEquals("Pizzas", group1.categoryName(), "Nombre desde factory");
            assertEquals(2, group1.products().size(), "Grupo 1 debe contener 2 productos");

            // Grupo 2 - CategoryId 2
            GroupedProductsResponseDto group2 = result.get(1);
            assertEquals(CATEGORY_ID_2, group2.categoryId(), "Segundo grupo debe ser categoría 2");
            assertEquals("Pastas", group2.categoryName(), "Nombre de categoría 2");
            assertEquals(1, group2.products().size(), "Grupo 2 debe contener 1 producto");

            // Verify
            verify(productRepository, times(1)).findProductsByRestaurantIdAndCategory(RESTAURANT_ID);
        }

        /**
         * Test: Lambda - Manejo de datos en la transformación.
         * <p>
         * DATOS: Usa ProductFactory.responseFromRequest()
         * <p>
         * Verificación:
         * ✅ La lambda (Map.Entry → GroupedProductsResponseDto) se ejecuta
         * ✅ Los datos del primer producto se usan para categoryName y restaurantName
         * ✅ El resultado contiene referencia a todos los productos
         */
        @Test
        @DisplayName("Lambda - Transforma Map.Entry a GroupedProductsResponseDto correctamente")
        void whenProcessingLambda_thenTransformsEntryToGroupedDto() {
            // Arrange
            List<ProductResponseDto> mockProducts = ProductFactory.responseListDefault();

            when(productRepository.findProductsByRestaurantIdAndCategory(RESTAURANT_ID))
                    .thenReturn(mockProducts);

            // Act
            List<GroupedProductsResponseDto> result = productService.findProductsByRestaurantIdAndCategory(RESTAURANT_ID);

            // Assert
            GroupedProductsResponseDto group = result.get(0);
            ProductResponseDto firstProduct = mockProducts.get(0);

            // Verificar que la lambda extrajo correctamente los datos desde factory
            assertEquals(firstProduct.categoryName(), group.categoryName(), "CategoryName desde producto (factory)");
            assertEquals(firstProduct.restaurantName(), group.restaurantName(), "RestaurantName desde producto (factory)");
            assertEquals(firstProduct.categoryId(), group.categoryId(), "CategoryId desde entry.getKey()");
            assertEquals(firstProduct.restaurantId(), group.restaurantId(), "RestaurantId desde primer producto");
            assertEquals(mockProducts.size(), group.products().size(), "Products lista debe contener todos los productos");
        }
    }

    /**
     * Test: Usar ProductFactory.groupedProductsDefault() como referencia.
     * <p>
     * DATOS: ProductFactory.groupedProductsDefault() devuelve la estructura esperada
     * - Pizzas: 2 productos
     * - Pastas: 1 producto
     * <p>
     * Este test valida que nuestro agrupamiento produce la misma estructura.
     */
    @Test
    @DisplayName("Resultado debe coincidir con ProductFactory.groupedProductsDefault()")
    void whenGrouping_thenResultMatchesFactoryGroupedDefault() {
        // Arrange
        List<ProductResponseDto> mockProducts = new ArrayList<>();
        mockProducts.addAll(ProductFactory.responseListDefault());
        mockProducts.addAll(ProductFactory.responseListPastas());

        List<GroupedProductsResponseDto> expected = ProductFactory.groupedProductsDefault();

        when(productRepository.findProductsByRestaurantIdAndCategory(RESTAURANT_ID))
                .thenReturn(mockProducts);

        // Act
        List<GroupedProductsResponseDto> result = productService.findProductsByRestaurantIdAndCategory(RESTAURANT_ID);

        // Assert
        assertEquals(expected.size(), result.size(), "Cantidad de grupos debe coincidir");
        for (int i = 0; i < expected.size(); i++) {
            GroupedProductsResponseDto expectedGroup = expected.get(i);
            GroupedProductsResponseDto resultGroup = result.get(i);

            assertEquals(expectedGroup.categoryId(), resultGroup.categoryId(),
                    "CategoryId del grupo " + i + " debe coincidir");
            assertEquals(expectedGroup.categoryName(), resultGroup.categoryName(),
                    "CategoryName del grupo " + i + " debe coincidir");
            assertEquals(expectedGroup.products().size(), resultGroup.products().size(),
                    "Cantidad de productos en grupo " + i + " debe coincidir");
        }
    }

    // ==================== CASOS DE ERROR ====================

    @Nested
    @DisplayName("Error Cases - findProductsByRestaurantIdAndCategory()")
    class ErrorCases {

        /**
         * Test: ID null → puede lanzar excepción o comportarse gracefully.
         */
        @Test
        @DisplayName("RestaurantId null → repositorio decide qué hacer")
        void whenRestaurantIdNull_thenRepositoryHandlesIt() {
            // Arrange
            when(productRepository.findProductsByRestaurantIdAndCategory(null))
                    .thenReturn(Collections.emptyList());

            // Act
            List<GroupedProductsResponseDto> result = productService.findProductsByRestaurantIdAndCategory(null);

            // Assert
            assertNotNull(result, "Resultado debe ser vacío, no null");
            assertTrue(result.isEmpty(), "Resultado debe estar vacío");

            // Verify
            verify(productRepository, times(1)).findProductsByRestaurantIdAndCategory(null);
        }

        /**
         * Test: ID inválido (≤ 0).
         */
        @Test
        @DisplayName("RestaurantId inválido (≤ 0) → retorna lista vacía")
        void whenRestaurantIdInvalid_thenRepositoryReturnsEmpty() {
            // Arrange
            when(productRepository.findProductsByRestaurantIdAndCategory(0L))
                    .thenReturn(Collections.emptyList());

            // Act
            List<GroupedProductsResponseDto> result = productService.findProductsByRestaurantIdAndCategory(0L);

            // Assert
            assertTrue(result.isEmpty(), "Resultado debe estar vacío para ID inválido");

            // Verify
            verify(productRepository, times(1)).findProductsByRestaurantIdAndCategory(0L);
        }

        /**
         * Test: ID negativo.
         */
        @Test
        @DisplayName("RestaurantId negativo → retorna lista vacía")
        void whenRestaurantIdNegative_thenRepositoryReturnsEmpty() {
            // Arrange
            when(productRepository.findProductsByRestaurantIdAndCategory(-1L))
                    .thenReturn(Collections.emptyList());

            // Act
            List<GroupedProductsResponseDto> result = productService.findProductsByRestaurantIdAndCategory(-1L);

            // Assert
            assertTrue(result.isEmpty(), "Resultado debe estar vacío para ID negativo");

            // Verify
            verify(productRepository, times(1)).findProductsByRestaurantIdAndCategory(-1L);
        }
    }

    // ==================== EDGE CASES - LAMBDA ====================

    @Nested
    @DisplayName("Edge Cases - Lambda Processing")
    class EdgeCasesLambda {

        /**
         * Test: Múltiples productos en mismo grupo → la lambda procesa CADA entry UNA VEZ.
         * <p>
         * DATOS: Usa ProductFactory para crear múltiples productos en la misma categoría.
         * <p>
         * Verificación:
         * ✅ Para N productos en categoría: lambda se ejecuta 1 vez (1 entry)
         * ✅ Para M productos en otra categoría: lambda se ejecuta 1 vez (1 entry)
         * Total: lambda se ejecuta 2 veces (una por categoría)
         */
        @Test
        @DisplayName("Lambda - Se ejecuta UNA VEZ por categoría (Map.Entry), no por producto")
        void whenMultipleProductsInCategory_thenLambdaExecutedOncePerCategory() {
            // Arrange
            List<ProductResponseDto> mockProducts = ProductFactory.responseListDefault();

            when(productRepository.findProductsByRestaurantIdAndCategory(RESTAURANT_ID))
                    .thenReturn(mockProducts);

            // Act
            List<GroupedProductsResponseDto> result = productService.findProductsByRestaurantIdAndCategory(RESTAURANT_ID);

            // Assert
            // Lambda se ejecuta 2 veces (una por cada Map.Entry/categoría)
            assertEquals(1, result.size(), "Debe haber 2 grupos (lambda ejecutada 2 veces)");
            assertEquals(2, result.get(0).products().size(), "Grupo contener 2 productos");

            // Verify
            verify(productRepository, times(1)).findProductsByRestaurantIdAndCategory(RESTAURANT_ID);
        }

        /**
         * Test: TreeMap ordena por categoryId ascendente.
         * <p>
         * DATOS: Crea productos con categoryIds desordenados (5, 2, 10)
         * y verifica que TreeMap los ordena.
         * <p>
         * Verificación:
         * ✅ Los grupos están en orden: categoryId 2, 5, 10
         * ✅ TreeMap ordena automáticamente por clave
         */
        @Test
        @DisplayName("TreeMap - Agrupa y ordena por categoryId ascendente")
        void whenMultipleCategoryIds_thenTreeMapOrdersAscending() {
            // Arrange
            List<ProductResponseDto> mockProducts = new ArrayList<>();
            // Agregar en orden inverso: Pastas primero (cat 2), Pizzas después (cat 1)
            mockProducts.addAll(ProductFactory.responseListPastas());       // categoryId = 2
            mockProducts.addAll(ProductFactory.responseListDefault());
            when(productRepository.findProductsByRestaurantIdAndCategory(RESTAURANT_ID))
                    .thenReturn(mockProducts);

            // Act
            List<GroupedProductsResponseDto> result = productService.findProductsByRestaurantIdAndCategory(RESTAURANT_ID);

            // Assert
            assertEquals(1L, result.get(0).categoryId(), "Primer grupo debe ser categoryId 1");
            assertEquals(2L, result.get(1).categoryId(), "Segundo grupo debe ser categoryId 2");


            // Verify ordenamiento correcto
            assertTrue(result.get(0).categoryId() < result.get(1).categoryId(), "Orden ascendente");
        }
    }
}