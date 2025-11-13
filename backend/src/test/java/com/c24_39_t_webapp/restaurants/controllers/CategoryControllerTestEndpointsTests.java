package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.CategoryFactory;
import com.c24_39_t_webapp.restaurants.services.ICategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para los endpoints de prueba del CategoryController
 * Verifica que el método testControllerGet retorna correctamente el mensaje de prueba (GET)
 * Verifica que el método testControllerPost retorna correctamente el mensaje con el parámetro (POST)
 * Estos endpoints no requieren autenticación (son públicos para facilitar testing)
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
@DisplayName("CategoryController - Test Endpoints (testMethod, testPostMethod)")
public class CategoryControllerTestEndpointsTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String RESTAURANT_EMAIL = "emailRestaurant@example.com";
    private static final String CATEGORY_ENDPOINT = "/api/category";
    private static final String TEST_GET_ENDPOINT = "/testMethod";
    private static final String TEST_POST_ENDPOINT = "/testPostMethod";
    private static final String TEST_NAME = "MiCategoria";
    private static final String TEST_USER_ROLE = "RESTAURANTE";
    private static final String VALID_EMAIL = "test@example.com";
    private static final long CATEGORY_ID = 1L;

    /**
     * MockMvc para simular peticiones HTTP al controlador
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mock del servicio de categorías (no se usa en endpoints de prueba, pero Spring lo requiere)
     */
    @MockitoBean
    private ICategoryService categoryService;

    /**
     * ObjectMapper para convertir objetos Java a JSON
     */
    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET TEST ENDPOINT ====================

    @Nested
    @DisplayName("GET /api/category/testMethod")
    class GetTestEndpointTests {

        /**
         * Test que verifica que el endpoint testMethod (GET) retorna correctamente
         * el mensaje de prueba indicando que el método GET funciona
         * Arrange: Sin configuración necesaria (endpoint no requiere parámetros ni autenticación)
         * Act & Assert: Realiza la petición GET y verifica el status 200 y el contenido esperado
         * Verify: Verifica que el servicio NO fue llamado (es un endpoint simple de prueba)
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/category/testMethod - Retorna 200 OK con mensaje de prueba")
        void whenCallTestGetMethod_thenReturnsOkWithTestMessage() throws Exception {
            log.info("Iniciando test de endpoint GET de prueba");

            // Act & Assert
            mockMvc.perform(get(CATEGORY_ENDPOINT + TEST_GET_ENDPOINT)
                    .with(user(RESTAURANT_EMAIL).roles(TEST_USER_ROLE))
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                    .andExpect(content().string(containsString("El metodo GET del controller de Categories funciona ok!")));

            // Verify que el servicio NO fue llamado
            verify(categoryService, never()).findAllCategories();
            verify(categoryService, never()).findCategoryById(any());
        }

        /**
         * Test que verifica la estructura exacta del mensaje retornado
         * Esto valida que la respuesta es exactamente la esperada
         * Arrange: Sin configuración necesaria
         * Act & Assert: Realiza la petición y verifica el contenido exacto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/category/testMethod - Valida el mensaje exacto retornado")
        void whenCallTestGetMethod_thenReturnsExactMessage() throws Exception {
            // Act & Assert
            mockMvc.perform(get(CATEGORY_ENDPOINT + TEST_GET_ENDPOINT)
                            .with(user(RESTAURANT_EMAIL).roles(TEST_USER_ROLE))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("El metodo GET del controller de Categories funciona ok!"));
        }
    }

    // ==================== POST TEST ENDPOINT ====================

    @Nested
    @DisplayName("POST /api/category/testPostMethod")
    class PostTestEndpointTests {

        /**
         * Test que verifica que el endpoint testPostMethod (POST) retorna correctamente
         * el mensaje de prueba incluyendo el parámetro name recibido
         * Arrange: Prepara el nombre a enviar en el body
         * Act & Assert: Realiza la petición POST y verifica el status 200 y el contenido con el parámetro incluido
         * Verify: Verifica que el servicio NO fue llamado (es un endpoint simple de prueba)
         *
         * @throws Exception
         */
        @Test
        @DisplayName("POST /api/category/testPostMethod - Retorna 200 OK con mensaje incluyendo el parámetro")
        void whenCallTestPostMethod_thenReturnsOkWithParameterIncluded() throws Exception {
            log.info("Iniciando test de endpoint POST de prueba");

            // Arrange
            String testNameParameter = TEST_NAME;

            // Act & Assert
            mockMvc.perform(post(CATEGORY_ENDPOINT + TEST_POST_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(testNameParameter)
                            .with(user(RESTAURANT_EMAIL).roles(TEST_USER_ROLE))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                    .andExpect(content().string(containsString("El metodo POST del controller de Categories funciona ok, " + testNameParameter + "!")));

            // Verify que el servicio NO fue llamado
            verify(categoryService, never()).findAllCategories();
            verify(categoryService, never()).findCategoryById(any());
        }

        /**
         * Test que verifica que el endpoint testPostMethod retorna el mensaje con el parámetro exacto
         * Arrange: Prepara un nombre específico
         * Act & Assert: Realiza la petición POST y verifica el mensaje exacto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("POST /api/category/testPostMethod - Valida el mensaje exacto con parámetro")
        void whenCallTestPostMethod_thenReturnsExactMessageWithParameter() throws Exception {
            // Arrange
            String testNameParameter = "PizzasSpeciales";

            // Act & Assert
            mockMvc.perform(post(CATEGORY_ENDPOINT + TEST_POST_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(testNameParameter)
                            .with(user(RESTAURANT_EMAIL).roles(TEST_USER_ROLE))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("El metodo POST del controller de Categories funciona ok, " + testNameParameter + "!"));
        }
    }
}