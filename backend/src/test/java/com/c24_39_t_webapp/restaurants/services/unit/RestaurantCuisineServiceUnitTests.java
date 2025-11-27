package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.CuisineResponseDto;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.models.RestaurantCuisine;
import com.c24_39_t_webapp.restaurants.repository.RestaurantCuisineRepository;
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
 * Test unitarios para RestaurantCuisineService.findAll()
 *
 * ✅ Cuisines vienen de EntityModelFactory
 * ✅ Happy path + edge cases
 *
 * Cobertura:
 * ✅ Obtener lista de cuisines
 * ✅ Lista vacía de cuisines
 * ✅ Múltiples cuisines en lista
 * ✅ CuisineResponseDto se mapea correctamente
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("RestaurantCuisineService - findAll()")
class RestaurantCuisineServiceUnitTests {

    private static final Long CUISINE_ID_1 = 1L;
    private static final String CUISINE_NAME_1 = "Italiana";
    private static final Long CUISINE_ID_2 = 2L;
    private static final String CUISINE_NAME_2 = "Española";
    private static final Long CUISINE_ID_3 = 3L;
    private static final String CUISINE_NAME_3 = "Francesa";

    @Mock
    private RestaurantCuisineRepository cuisineRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.RestaurantCuisineServiceImpl cuisineService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - findAll()")
    class SuccessCases {

        private List<RestaurantCuisine> cuisineList;

        @BeforeEach
        void setUp() {
            // ✅ Crear lista de cuisines usando factory
            RestaurantCuisine cuisine1 = EntityModelFactory.restaurantCuisine(CUISINE_ID_1, CUISINE_NAME_1);
            RestaurantCuisine cuisine2 = EntityModelFactory.restaurantCuisine(CUISINE_ID_2, CUISINE_NAME_2);
            RestaurantCuisine cuisine3 = EntityModelFactory.restaurantCuisine(CUISINE_ID_3, CUISINE_NAME_3);

            cuisineList = Arrays.asList(cuisine1, cuisine2, cuisine3);
        }

        /**
         * Test: Obtener lista de cuisines
         *
         * Verificación:
         * ✅ CuisineRepository.findAll() se llamó
         * ✅ Retorna lista con datos correctos
         * ✅ Size correcto
         */
        @Test
        @DisplayName("Obtener lista de cuisines → Retorna lista correcta")
        void whenGettingAllCuisines_thenReturnsListSuccessfully() {
            // Arrange
            when(cuisineRepository.findAll())
                    .thenReturn(cuisineList);

            // Act
            List<CuisineResponseDto> result = cuisineService.findAll();

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(3, result.size(), "Debe retornar 3 cuisines");
            assertEquals(CUISINE_NAME_1, result.get(0).name(), "Primera cuisine debe ser Italiana");
            assertEquals(CUISINE_NAME_2, result.get(1).name(), "Segunda cuisine debe ser Española");
            assertEquals(CUISINE_NAME_3, result.get(2).name(), "Tercera cuisine debe ser Francesa");

            // Verify
            verify(cuisineRepository, times(1)).findAll();
        }

        /**
         * Test: Mapeo correcto a CuisineResponseDto
         *
         * Verificación:
         * ✅ ID se mapea correctamente
         * ✅ Name se mapea correctamente
         */
        @Test
        @DisplayName("Mapeo a CuisineResponseDto → Datos correctos")
        void whenMappingToDtoList_thenDataIsMappedCorrectly() {
            // Arrange
            when(cuisineRepository.findAll())
                    .thenReturn(cuisineList);

            // Act
            List<CuisineResponseDto> result = cuisineService.findAll();

            // Assert
            for (int i = 0; i < result.size(); i++) {
                CuisineResponseDto dto = result.get(i);
                RestaurantCuisine entity = cuisineList.get(i);

                assertEquals(entity.getId(), dto.id(), "ID debe coincidir");
                assertEquals(entity.getName(), dto.name(), "Name debe coincidir");
            }

            // Verify
            verify(cuisineRepository, times(1)).findAll();
        }

        /**
         * Test: Una única cuisine en la lista
         *
         * Verificación:
         * ✅ Retorna lista con 1 elemento
         */
        @Test
        @DisplayName("Una única cuisine → Retorna lista con 1 elemento")
        void whenOnlyOneCuisine_thenReturnsListWithOneElement() {
            // Arrange
            RestaurantCuisine singleCuisine = EntityModelFactory.restaurantCuisine(CUISINE_ID_1, CUISINE_NAME_1);
            List<RestaurantCuisine> singleList = Collections.singletonList(singleCuisine);

            when(cuisineRepository.findAll())
                    .thenReturn(singleList);

            // Act
            List<CuisineResponseDto> result = cuisineService.findAll();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size(), "Debe retornar 1 cuisine");
            assertEquals(CUISINE_NAME_1, result.get(0).name());

            // Verify
            verify(cuisineRepository, times(1)).findAll();
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases - findAll()")
    class EdgeCases {

        /**
         * Test: Lista vacía de cuisines
         *
         * Verificación:
         * ✅ Retorna Collections.emptyList()
         * ✅ No es null
         * ✅ Size es 0
         */
        @Test
        @DisplayName("Sin cuisines → Retorna lista vacía")
        void whenNoCuisines_thenReturnsEmptyList() {
            // Arrange
            when(cuisineRepository.findAll())
                    .thenReturn(new ArrayList<>());

            // Act
            List<CuisineResponseDto> result = cuisineService.findAll();

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertTrue(result.isEmpty(), "Lista debe estar vacía");
            assertEquals(0, result.size(), "Size debe ser 0");

            // Verify
            verify(cuisineRepository, times(1)).findAll();
        }

        /**
         * Test: Lista grande de cuisines
         *
         * Verificación:
         * ✅ Retorna lista con muchos elementos
         * ✅ Todos se mapean correctamente
         */
        @Test
        @DisplayName("Muchas cuisines → Retorna lista completa")
        void whenManyCustomers_thenReturnsCompleteList() {
            // Arrange
            List<RestaurantCuisine> largeCuisineList = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                RestaurantCuisine cuisine = EntityModelFactory.restaurantCuisine(
                        (long) i,
                        "Cuisine" + i
                );
                largeCuisineList.add(cuisine);
            }

            when(cuisineRepository.findAll())
                    .thenReturn(largeCuisineList);

            // Act
            List<CuisineResponseDto> result = cuisineService.findAll();

            // Assert
            assertNotNull(result);
            assertEquals(10, result.size(), "Debe retornar 10 cuisines");

            // Verificar primero y último
            assertEquals("Cuisine1", result.get(0).name());
            assertEquals("Cuisine10", result.get(9).name());

            // Verify
            verify(cuisineRepository, times(1)).findAll();
        }
    }
}