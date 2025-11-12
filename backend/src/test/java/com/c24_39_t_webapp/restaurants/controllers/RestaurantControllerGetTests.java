package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.exception.ResourceNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.RestaurantFactory;
import com.c24_39_t_webapp.restaurants.services.IRestaurantService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para el endpoint de obtención de restaurantes (GET)
 * Verifica que al obtener todos los restaurantes, se retorna el código 200 OK con la lista de restaurantes
 * Verifica que al obtener todos los restaurantes, si no los hay se obtiene una lista vacía
 * Verifica que al obtener un restaurante por ID válido, se retorna 200 OK con los datos del restaurante
 * También verifica el caso de error:
 * - RestaurantNotFoundException: cuando el restaurante no existe. Retorna 404 Not Found
 * - UnauthorizedAccessException: cuando el usuario no tiene permiso. Retorna 403 Forbidden
 * - Petición sin autenticación: retorna 401 Unauthorized
 * Patrón AAA: Arrange, Act, Assert
 */
@WebMvcTest(
        controllers = RestaurantController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@Slf4j
@DisplayName("RestaurantController - GET /api/restaurant/")
public class RestaurantControllerGetTests {
    /**
     * Constantes para evitar el uso de "magic strings" hardcodeadas en los tests
     */
    private static final String RESTAURANT_ENDPOINT = "/api/restaurant";
    private static final String VALID_EMAIL = "test@example.com";
    private static final long OWNER_ID = 1L;
    /**
     * MockMvc para simular peticiones HTTP al controlador
     */
    @Autowired
    private MockMvc mockMvc;
    /**
     * Mock del servicio de restaurantes
     */
    @MockitoBean
    private IRestaurantService restaurantService;
    /**
     * ObjectMapper para convertir objetos Java a JSON
     */
    @Autowired
    private ObjectMapper objectMapper;

    // Setup compartido para get
    //Restaurante esperado en las respuestas
    private RestaurantResponseDto expectedRestaurantResponse;
    //Lista de restaurantes esperada en las respuestas
    private List<RestaurantResponseDto> mockRestaurantsList;
    //ID de restaurante esperado en las respuestas
    private Long restId;

    @BeforeEach
    void setUp() {
        RestaurantRequestDto newRequestDto = RestaurantFactory.defaultRequest(1, "emailTest1@email.com" );
        expectedRestaurantResponse = RestaurantFactory.responseFromRequest(newRequestDto,1);
        mockRestaurantsList = RestaurantFactory.responseListDefault();
        restId = expectedRestaurantResponse.rst_id();
    }

    @Nested
    @DisplayName("GetAll Cases")
    class GetAllCases {
        /**
         * Test que verifica que al intentar obtener todos los restaurantes retorna una lista con todos los restaurantes
         * Arrange: Configura el mock del servicio para retornar la lista de restaurantes
         * Act & Assert: Realiza la petición GetAll y verifica el status 200 y el retorno de la lista correcta
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET All /api/restaurant/{id} Retorna 200 OK con lista de todos los restaurantes")
        void whenGetAllRestaurants_thenReturnsOkWithList() throws Exception {
            // Arrange
            when(restaurantService.findAll()).thenReturn(mockRestaurantsList);

            // Act & Assert
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/all")
                            .with(user(VALID_EMAIL).roles("CLIENTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].rst_id").value(1L))
                    .andExpect(jsonPath("$[0].name").value("Atlántico"))
                    .andExpect(jsonPath("$[1].rst_id").value(2L))
                    .andExpect(jsonPath("$[1].name").value("La Paella"));

            // Verify
            verify(restaurantService, times(1)).findAll();
        }

        /**
         * Test que verifica que al intentar obtener todos los restaurantes cundo no los hay
         * retorna una lista vacía
         * Arrange: Configura el mock del servicio para retornar una lista vacía
         * Act & Assert: Realiza la petición GetAll y verifica el status 200 y el retorno de una lista vacía
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET All /api/restaurant/{id} Retorna 200 OK con lista VACÍA si no hay restaurantes")
        void whenNoRestaurants_thenReturnsEmptyList() throws Exception {
            // Arrange
            when(restaurantService.findAll()).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/all")
                            .with(user(VALID_EMAIL).roles("CLIENTE")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));  // ← Array vacío

            verify(restaurantService, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("GetById Success Cases")
    class GetByIdSuccess {
        /**
         * Test que verifica que al intentar obtener un restaurante con ID válido
         * Arrange: Configura el mock del servicio para retornar en RestauranteResponseDto esperado
         * Act & Assert: Realiza la petición Get by ID y verifica el status 200 y el contenido de la respuesta
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET by ID /api/restaurant/{id} - Retorna 200 con los datos del restaurante")
        void whenGetByValidId_thenReturnsOk() throws Exception {
            // Arrange
            when(restaurantService.findById(restId))
                    .thenReturn(expectedRestaurantResponse);

            // Act & Assert
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/" + restId)
                            .with(user(VALID_EMAIL).roles("CLIENTE"))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.rst_id").value(restId))
                    .andExpect(jsonPath("$.name").value("Atlántico"));

            verify(restaurantService, times(1)).findById(restId);
        }
    }

    @Nested
    @DisplayName("GetById Error Cases")
    class GetByIdErrors {
        /**
         * Test que verifica que al intentar obtener un restaurante sin autenticación, se retorna 401 Unauthorized
         * Act & Assert: Realiza la petición Get by ID sin usuario y verifica el status 401
         * Verify: Verifica que el servicio NUNCA se llamó
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET by ID /api/restaurant/{id} - Retorna 401 Unauthorized Rechaza GET sin autenticación")
        void whenGetWithoutAuthentication_thenUnauthorized() throws Exception {
            // Act & Assert
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/" + restId)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify que el servicio NUNCA se llamó
            verify(restaurantService, never()).findById(eq(restId));
        }

        /**
         * Test que verifica que al intentar obtener un restaurante inexistente,
         * se lanza RestaurantNotFoundException y se retorna 404 Not Found
         * Arrange: Configura el mock del servicio para que lance RestaurantNotFoundException
         * Act & Assert: Realiza la petición GET by ID y verifica el status 404 y el mensaje de error
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET by ID /api/restaurant/{id} - Retorna 404 RestaurantNotFoundException si el restaurante no existe")
        void whenRestaurantNotFound_thenReturns404() throws Exception {
            // Arrange
            when(restaurantService.findById(999L))
                    .thenThrow(new RestaurantNotFoundException("Restaurante no encontrado"));

            // Act & Assert
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/999")
                            .with(user(VALID_EMAIL).roles("CLIENTE"))
                            .with(csrf()))
                    .andExpect(status().isNotFound());

            verify(restaurantService, times(1)).findById(999L);
        }

        /**
         * Test que verifica que al intentar obtener un restaurante sin ser el dueño,
         * se lanza UnauthorizedAccessException y se retorna 403 Forbidden
         * Arrange: Configura el mock del servicio para que lance UnauthorizedAccessException
         * Act & Assert: Realiza la petición Get by ID y verifica el status 403
         * Verify: Verifica que el servicio se llamó una vez con el ID correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET by ID /api/restaurant/{id} - Retorna 403 Forbidden Rechaza GET si no es el dueño")
        void whenGetNotRestaurantOwner_thenReturnsForbidden() throws Exception {
            // Arrange
            when(restaurantService.findById(restId))
                    .thenThrow(new UnauthorizedAccessException("No tienes permiso para este restaurante."));

            // Act & Assert
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/" + restId)
                            .with(user("otro@inexistente.com").roles("CLIENTE"))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(restaurantService, times(1)).findById(restId);

        }
    }

    @Nested
    @DisplayName("GetByOwnerId Success Cases")
    class GetByOwnerIdSuccess {

        @Test
        @DisplayName("GET by Owner /api/restaurant/byOwnerId/{id}  Retorna 200 con lista de restaurantes del dueño")
        void whenOwnerHasRestaurants_thenReturnsOkWithList() throws Exception {
            // Arrange
            when(restaurantService.findRestaurantsByOwnerId(OWNER_ID))
                    .thenReturn(mockRestaurantsList);

            // Act & Assert
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/byOwnerId/" + OWNER_ID)
                            .with(user(VALID_EMAIL).roles("RESTAURANTE")))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].rst_id").value(1L))
                    .andExpect(jsonPath("$[0].name").value("Atlántico"))
                    .andExpect(jsonPath("$[1].rst_id").value(2L))
                    .andExpect(jsonPath("$[1].name").value("La Paella"));

            verify(restaurantService, times(1)).findRestaurantsByOwnerId(OWNER_ID);
        }

        @Test
        @DisplayName("GET by Owner /api/restaurant/byOwnerId/{id} - Retorna 200 con lista vacía si el dueño no tiene restaurantes")
        void whenOwnerHasNoRestaurants_thenReturnsEmptyList() throws Exception {
            // Arrange
            when(restaurantService.findRestaurantsByOwnerId(999L))
                    .thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/byOwnerId/999")
                            .with(user(VALID_EMAIL).roles("RESTAURANTE")))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(restaurantService, times(1)).findRestaurantsByOwnerId(999L);
        }
    }

    @Nested
    @DisplayName("GetByOwnerId Error Cases")
    class GetByOwnerIdErrors {

        @Test
        @DisplayName("Fail GET by Owner /api/restaurant/byOwnerId/{id} - Retorna 404 si el dueño no existe")
        void whenOwnerNotFound_thenReturns404() throws Exception {
            // Arrange
            when(restaurantService.findRestaurantsByOwnerId(999L))
                    .thenThrow(new ResourceNotFoundException("Dueño no encontrado con ID: 999"));

            // Act & Assert
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/byOwnerId/999")
                            .with(user(VALID_EMAIL).roles("RESTAURANTE")))
                    .andExpect(status().isNotFound());

            verify(restaurantService, times(1)).findRestaurantsByOwnerId(999L);
        }

        @Test
        @DisplayName("Fail GET by Owner /api/restaurant/byOwnerId/{id} - Retorna 500 si el servicio lanza excepción")
        void whenServiceThrowsException_thenReturnsInternalError() throws Exception {
            // Arrange
            when(restaurantService.findRestaurantsByOwnerId(any()))
                    .thenThrow(new RuntimeException("Error en la BD"));

            // Act & Assert
            mockMvc.perform(get(RESTAURANT_ENDPOINT + "/byOwnerId/" + OWNER_ID)
                            .with(user(VALID_EMAIL).roles("RESTAURANTE")))
                    .andExpect(status().isInternalServerError());

            verify(restaurantService, times(1)).findRestaurantsByOwnerId(OWNER_ID);
        }
    }
}
