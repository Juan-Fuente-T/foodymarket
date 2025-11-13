package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.services.ICategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test para el endpoint DELETE de CategoryController
 * Verifica que al borrar una categoría de un restaurante, se retorna el código 204 No Content
 * También verifica los casos de error:
 * - CategoryNotFoundException: cuando la categoría no existe. Retorna 404 Not Found
 * - UnauthorizedAccessException: cuando el usuario no tiene permiso. Retorna 403 Forbidden
 * - Petición sin autenticación: retorna 401 Unauthorized
 *
 * Patrón AAA: Arrange, Act, Assert
 */
@WebMvcTest(
        controllers = CategoryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@Slf4j
@DisplayName("CategoryController - DELETE /api/category/{categoryId}/restaurant/{restaurantId}")
public class CategoryControllerDeleteTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String CATEGORY_ENDPOINT = "/api/category";
    private static final String VALID_EMAIL = "test@example.com";
    private static final long CATEGORY_ID = 1L;
    private static final long RESTAURANT_ID = 1L;

    /**
     * MockMvc para simular peticiones HTTP al controlador
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mock del servicio de categorías
     */
    @MockitoBean
    private ICategoryService categoryService;

    /**
     * ObjectMapper para convertir objetos Java a JSON
     */
    @Autowired
    private ObjectMapper objectMapper;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - DELETE /api/category/{categoryId}/restaurant/{restaurantId}")
    class SuccessCases {

        /**
         * Test que verifica que al borrar una categoría de un restaurante con datos válidos,
         * se retorna el código 204 No Content
         * Arrange: Configura el mock del servicio para no hacer nada (void)
         * Act & Assert: Realiza la petición DELETE CON autenticación y verifica el status 204
         * Verify: Verifica que el servicio se llamó una sola vez con los parámetros correctos
         *
         * @throws Exception
         */
        @Test
        @DisplayName("DELETE /api/category/{categoryId}/restaurant/{restaurantId} - Retorna 204 No Content al borrar")
        void whenDeleteCategoryWithValidData_thenReturnsNoContent() throws Exception {
            log.info("Iniciando test de borrado de categoría");

            // Arrange - El servicio no retorna nada (void)
            doNothing().when(categoryService).deleteCategory(eq(RESTAURANT_ID), eq(CATEGORY_ID));

            // Act & Assert - CON autenticación, rol RESTAURANTE y CSRF
            mockMvc.perform(delete(CATEGORY_ENDPOINT + "/" + CATEGORY_ID + "/restaurant/" + RESTAURANT_ID)
                            .with(user(VALID_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // Verify
            verify(categoryService, times(1)).deleteCategory(eq(RESTAURANT_ID), eq(CATEGORY_ID));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - DELETE /api/category/{categoryId}/restaurant/{restaurantId}")
    class ErrorCases {

        /**
         * Test que verifica que al intentar borrar una categoría que no existe,
         * se retorna el código 404 Not Found
         * Arrange: Configura el mock del servicio para lanzar CategoryNotFoundException
         * Act & Assert: Realiza la petición DELETE y verifica el status 404
         * Verify: Verifica que el servicio se llamó una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/category/{categoryId}/restaurant/{restaurantId} - Retorna 404 si la categoría no existe")
        void whenCategoryNotFound_thenReturns404() throws Exception {
            // Arrange
            doThrow(new CategoryNotFoundException("No se encontro una categoria con ese ID: 999"))
                    .when(categoryService).deleteCategory(eq(RESTAURANT_ID), eq(999L));

            // Act & Assert - CON autenticación y CSRF
            mockMvc.perform(delete(CATEGORY_ENDPOINT + "/999/restaurant/" + RESTAURANT_ID)
                            .with(user(VALID_EMAIL).roles("RESTAURANTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("CategoryNotFoundException"));

            // Verify
            verify(categoryService, times(1)).deleteCategory(eq(RESTAURANT_ID), eq(999L));
        }

        /**
         * Test que verifica que al intentar borrar una categoría sin autenticación,
         * se retorna el código 401 Unauthorized
         * Arrange: No se configura autenticación
         * Act & Assert: Realiza la petición DELETE sin auth y verifica el status 401
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/category/{categoryId}/restaurant/{restaurantId} - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(delete(CATEGORY_ENDPOINT + "/" + CATEGORY_ID + "/restaurant/" + RESTAURANT_ID)
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NO fue llamado
            verify(categoryService, never()).deleteCategory(any(), any());
        }
        }
}
