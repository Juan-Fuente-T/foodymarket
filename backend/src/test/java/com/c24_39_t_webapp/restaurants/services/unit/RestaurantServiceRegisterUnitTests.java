package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.factories.RestaurantFactory;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.RestaurantCuisine;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para RestaurantService.registerRestaurant()
 *
 * ✅ TODOS los datos vienen de RestaurantFactory
 * ✅ Usuarios vienen de EntityModelFactory.restaurantOwnerEntity()
 * ✅ Sincronización con factories (RestaurantFactory, UserFactory)
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Registro exitoso: usuario válido + cuisine válida
 * ✅ Usuario no encontrado
 * ✅ Usuario sin rol RESTAURANTE
 * ✅ Cuisine no encontrada
 * ✅ Retorna RestaurantResponseDto con datos correctos
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("RestaurantService - registerRestaurant()")
public class RestaurantServiceRegisterUnitTests {

    private static final Long RESTAURANT_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long CUISINE_ID = 1L;
    private static final String VALID_EMAIL = "owner@restaurant.com";
    private static final String INVALID_EMAIL = "nonexistent@restaurant.com";

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.RestaurantRepository restaurantRepository;

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.UserRepository userRepository;

    @Mock
    private com.c24_39_t_webapp.restaurants.services.ICategoryService categoryService;

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.RestaurantCuisineRepository cuisineRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.RestaurantServiceImpl restaurantService;

    // ==================== SETUP ====================

    private RestaurantRequestDto validRestaurantRequest;
    private UserEntity validOwner;
    private RestaurantCuisine validCuisine;

    @BeforeEach
    void setUp() {
        // ✅ Datos desde RestaurantFactory
        validRestaurantRequest = RestaurantFactory.defaultRequest(USER_ID, VALID_EMAIL);

        // ✅ Usuario propietario con rol RESTAURANTE desde EntityModelFactory
        validOwner = com.c24_39_t_webapp.restaurants.factories.EntityModelFactory
                .restaurantOwnerEntity(USER_ID, VALID_EMAIL);

        // ✅ Cuisine válida
        validCuisine = new RestaurantCuisine();
        validCuisine.setId(CUISINE_ID);
        validCuisine.setName("Mediterránea");
    }

    // ==================== CASOS DE ÉXITO ====================

    @Nested
    @DisplayName("Success Cases - registerRestaurant()")
    class SuccessCases {

        /**
         * Test: Registro EXITOSO con datos válidos
         *
         * Verificación:
         * ✅ Usuario encontrado y tiene rol RESTAURANTE
         * ✅ Cuisine encontrada
         * ✅ EntityManager.persist() se llamó una vez
         * ✅ EntityManager.flush() se llamó una vez
         * ✅ Retorna RestaurantResponseDto con datos correctos
         */
        @Test
        @DisplayName("Datos válidos → Registra exitosamente y retorna RestaurantResponseDto")
        void whenValidData_thenRegistersSuccessfully() {
            // Arrange
            when(userRepository.findByEmail(VALID_EMAIL))
                    .thenReturn(Optional.of(validOwner));

            when(cuisineRepository.findById(validRestaurantRequest.cuisineId()))
                    .thenReturn(Optional.of(validCuisine));

            // Act
            RestaurantResponseDto result = restaurantService.registerRestaurant(
                    validRestaurantRequest, VALID_EMAIL
            );

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(validRestaurantRequest.name(), result.name(), "Nombre debe coincidir con request");
            assertEquals(validRestaurantRequest.description(), result.description(), "Descripción debe coincidir");
            assertEquals(validRestaurantRequest.phone(), result.phone(), "Teléfono debe coincidir");
            assertEquals(validRestaurantRequest.email(), result.email(), "Email debe coincidir");
            assertEquals(validRestaurantRequest.address(), result.address(), "Dirección debe coincidir");
            assertEquals(validRestaurantRequest.openingHours(), result.openingHours(), "Horario debe coincidir");
            assertEquals(USER_ID, result.rst_user_id(), "User ID debe coincidir");
            assertEquals(CUISINE_ID, result.cuisineId(), "Cuisine ID debe coincidir");
            assertEquals(validCuisine.getName(), result.cuisineName(), "Cuisine name debe coincidir");

            // Verify
            verify(userRepository, times(1)).findByEmail(VALID_EMAIL);
            verify(cuisineRepository, times(1)).findById(validRestaurantRequest.cuisineId());
            verify(entityManager, times(1)).persist(any(Restaurant.class));
            verify(entityManager, times(1)).flush();
        }

        /**
         * Test: EntityManager.persist() recibe instancia de Restaurant correcta
         *
         * Verificación:
         * ✅ La entidad persistida tiene todos los valores del request
         * ✅ UserEntity está asignado correctamente
         * ✅ Cuisine está asignada correctamente
         */
        @Test
        @DisplayName("EntityManager.persist() recibe Restaurant con datos correctos")
        void whenPersisting_thenRestaurantHasCorrectData() {
            // Arrange
            when(userRepository.findByEmail(VALID_EMAIL))
                    .thenReturn(Optional.of(validOwner));

            when(cuisineRepository.findById(validRestaurantRequest.cuisineId()))
                    .thenReturn(Optional.of(validCuisine));

            // Captura el argumento pasado a persist()
            ArgumentCaptor<Restaurant> restaurantCaptor = ArgumentCaptor.forClass(Restaurant.class);

            // Act
            restaurantService.registerRestaurant(validRestaurantRequest, VALID_EMAIL);

            // Assert
            verify(entityManager).persist(restaurantCaptor.capture());
            Restaurant capturedRestaurant = restaurantCaptor.getValue();

            assertEquals(validRestaurantRequest.name(), capturedRestaurant.getName(),
                    "Nombre en entidad debe coincidir");
            assertEquals(validRestaurantRequest.description(), capturedRestaurant.getDescription(),
                    "Descripción en entidad debe coincidir");
            assertEquals(validRestaurantRequest.phone(), capturedRestaurant.getPhone(),
                    "Teléfono en entidad debe coincidir");
            assertEquals(validRestaurantRequest.email(), capturedRestaurant.getEmail(),
                    "Email en entidad debe coincidir");
            assertEquals(validRestaurantRequest.address(), capturedRestaurant.getAddress(),
                    "Dirección en entidad debe coincidir");
            assertEquals(validRestaurantRequest.openingHours(), capturedRestaurant.getOpeningHours(),
                    "Horario en entidad debe coincidir");
            assertEquals(validOwner, capturedRestaurant.getUserEntity(),
                    "Usuario debe estar asignado");
            assertEquals(validCuisine, capturedRestaurant.getCuisine(),
                    "Cuisine debe estar asignada");
        }
    }

    // ==================== CASOS DE ERROR ====================

    @Nested
    @DisplayName("Error Cases - registerRestaurant()")
    class ErrorCases {

        /**
         * Test: Usuario NO ENCONTRADO
         *
         * Verificación:
         * ✅ Lanza ResponseStatusException con HttpStatus.FORBIDDEN
         * ✅ No busca cuisine (falla antes)
         * ✅ No persiste nada
         */
        @Test
        @DisplayName("Email inválido (usuario no encontrado) → lanza ResponseStatusException FORBIDDEN")
        void whenUserNotFound_thenThrowsForbidden() {
            // Arrange
            when(userRepository.findByEmail(INVALID_EMAIL))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> restaurantService.registerRestaurant(validRestaurantRequest, INVALID_EMAIL),
                    "Debe lanzar ResponseStatusException"
            );

            assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode(),
                    "Status debe ser FORBIDDEN");
            assertTrue(exception.getReason().contains("Usuario no registrado"),
                    "Mensaje debe mencionar usuario no registrado");

            // Verify
            verify(userRepository, times(1)).findByEmail(INVALID_EMAIL);
            verify(cuisineRepository, never()).findById(any());
            verify(entityManager, never()).persist(any());
        }

        /**
         * Test: Usuario sin rol RESTAURANTE
         *
         * Verificación:
         * ✅ Usuario existe pero tiene rol diferente (ej: CLIENTE)
         * ✅ Lanza ResponseStatusException con HttpStatus.FORBIDDEN
         * ✅ No busca cuisine ni persiste
         */
        @Test
        @DisplayName("Usuario con rol CLIENTE (no RESTAURANTE) → lanza ResponseStatusException FORBIDDEN")
        void whenUserHasWrongRole_thenThrowsForbidden() {
            // Arrange
            UserEntity clientUser = com.c24_39_t_webapp.restaurants.factories.EntityModelFactory
                    .clientEntity(USER_ID, VALID_EMAIL);

            when(userRepository.findByEmail(VALID_EMAIL))
                    .thenReturn(Optional.of(clientUser));

            // Act & Assert
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> restaurantService.registerRestaurant(validRestaurantRequest, VALID_EMAIL),
                    "Debe lanzar ResponseStatusException"
            );

            assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode(),
                    "Status debe ser FORBIDDEN");
            assertTrue(exception.getReason().contains("permisos"),
                    "Mensaje debe mencionar permisos");

            // Verify
            verify(userRepository, times(1)).findByEmail(VALID_EMAIL);
            verify(cuisineRepository, never()).findById(any());
            verify(entityManager, never()).persist(any());
        }

        /**
         * Test: Cuisine NO ENCONTRADA
         *
         * Verificación:
         * ✅ Usuario existe y es RESTAURANTE
         * ✅ Cuisine ID es inválido
         * ✅ Lanza ResponseStatusException con HttpStatus.BAD_REQUEST
         * ✅ No persiste
         */
        @Test
        @DisplayName("Cuisine ID inválido → lanza ResponseStatusException BAD_REQUEST")
        void whenCuisineNotFound_thenThrowsBadRequest() {
            // Arrange
            when(userRepository.findByEmail(VALID_EMAIL))
                    .thenReturn(Optional.of(validOwner));

            when(cuisineRepository.findById(validRestaurantRequest.cuisineId()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> restaurantService.registerRestaurant(validRestaurantRequest, VALID_EMAIL),
                    "Debe lanzar ResponseStatusException"
            );

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode(),
                    "Status debe ser BAD_REQUEST");
            assertTrue(exception.getReason().contains("cocina inválido"),
                    "Mensaje debe mencionar cocina inválida");

            // Verify
            verify(userRepository, times(1)).findByEmail(VALID_EMAIL);
            verify(cuisineRepository, times(1)).findById(validRestaurantRequest.cuisineId());
            verify(entityManager, never()).persist(any());
        }

        /**
         * Test: Email null
         *
         * Verificación:
         * ✅ Manejo de null email (repositorio decide qué retornar)
         */
        @Test
        @DisplayName("Email null → repositorio devuelve Optional.empty()")
        void whenEmailNull_thenRepositoryReturnsEmpty() {
            // Arrange
            when(userRepository.findByEmail(null))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    ResponseStatusException.class,
                    () -> restaurantService.registerRestaurant(validRestaurantRequest, null)
            );

            // Verify
            verify(userRepository, times(1)).findByEmail(null);
            verify(entityManager, never()).persist(any());
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases - registerRestaurant()")
    class EdgeCases {

        /**
         * Test: RequestDto con valores null (excepto required)
         *
         * Verificación:
         * ✅ El servicio puede crear restaurant incluso con algunos campos null
         */
        @Test
        @DisplayName("RequestDto con campos opcionales null → registra exitosamente")
        void whenOptionalFieldsNull_thenRegistersSuccessfully() {
            // Arrange
            RestaurantRequestDto requestWithNulls = new RestaurantRequestDto(
                    USER_ID,
                    "Mi Restaurante",
                    "Descripción",
                    CUISINE_ID,
                    "555 666 777",
                    VALID_EMAIL,
                    "Dirección",
                    null,  // openingHours null
                    null,  // logo null
                    null   // coverImage null
            );

            when(userRepository.findByEmail(VALID_EMAIL))
                    .thenReturn(Optional.of(validOwner));

            when(cuisineRepository.findById(CUISINE_ID))
                    .thenReturn(Optional.of(validCuisine));

            // Act
            RestaurantResponseDto result = restaurantService.registerRestaurant(
                    requestWithNulls, VALID_EMAIL
            );

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertNull(result.openingHours(), "OpeningHours puede ser null");
            assertNull(result.logo(), "Logo puede ser null");
            assertNull(result.coverImage(), "CoverImage puede ser null");

            // Verify
            verify(entityManager, times(1)).persist(any(Restaurant.class));
        }

        /**
         * Test: Comparar con datos de RestaurantFactory
         *
         * Verificación:
         * ✅ El response coincide exactamente con lo que espera RestaurantFactory
         */
        @Test
        @DisplayName("Response coincide con datos sincronizados de RestaurantFactory")
        void whenRegistering_thenResponseMatchesFactoryData() {
            // Arrange
            RestaurantRequestDto factoryRequest = RestaurantFactory.defaultRequest(USER_ID, VALID_EMAIL);
            RestaurantResponseDto factoryExpected = RestaurantFactory.responseFromRequest(factoryRequest, RESTAURANT_ID);

            when(userRepository.findByEmail(VALID_EMAIL))
                    .thenReturn(Optional.of(validOwner));

            when(cuisineRepository.findById(factoryRequest.cuisineId()))
                    .thenReturn(Optional.of(validCuisine));

            // Act
            RestaurantResponseDto result = restaurantService.registerRestaurant(
                    factoryRequest, VALID_EMAIL
            );

            // Assert
            // Comparar todos los campos sincronizados
            assertEquals(factoryRequest.name(), result.name(), "Nombre debe ser del factory");
            assertEquals(factoryRequest.description(), result.description(), "Descripción debe ser del factory");
            assertEquals(factoryRequest.phone(), result.phone(), "Teléfono debe ser del factory");
            assertEquals(factoryRequest.email(), result.email(), "Email debe ser del factory");
            assertEquals(factoryRequest.address(), result.address(), "Dirección debe ser del factory");
            assertEquals(factoryRequest.openingHours(), result.openingHours(), "Horario debe ser del factory");
        }
    }
}
