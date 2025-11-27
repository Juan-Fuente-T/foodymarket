package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.factories.RestaurantFactory;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.RestaurantCuisine;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para RestaurantService - métodos FIND/GET
 *
 * ✅ TODOS los datos vienen de RestaurantFactory
 * ✅ Usuarios vienen de EntityModelFactory
 * ✅ Sincronización con factories
 * ✅ Happy path + error cases
 *
 * Métodos testeados:
 * - findAll()
 * - findById(Long id)
 * - findRestaurantsByOwnerId(Long ownerId)
 * - findByIdFetchingCategories(Long restaurantId)
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("RestaurantService - FIND/GET Methods")
public class RestaurantServiceFindUnitTests {

    private static final Long RESTAURANT_ID = 1L;
    private static final Long RESTAURANT_ID_2 = 2L;
//    private static final Long USER_ID = 1L;
    private static final Long OWNER_ID = 1L;
//    private static final Long UNAUTHORIZED_USER_ID = 2L;
    private static final Long CUISINE_ID = 1L;
    private static final String OWNER_EMAIL = "owner@restaurant.com";
    private static final String UNAUTHORIZED_EMAIL = "unauthorized@restaurant.com";

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.RestaurantRepository restaurantRepository;

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.UserRepository userRepository;

    @Mock
    private com.c24_39_t_webapp.restaurants.services.ICategoryService categoryService;

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.RestaurantCuisineRepository cuisineRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.RestaurantServiceImpl restaurantService;

    // ==================== SETUP ====================

    private Restaurant restaurant1;
    private Restaurant restaurant2;
    private UserEntity owner;
    private RestaurantCuisine cuisine;
    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        // ✅ SOLO INICIALIZAR ENTIDADES - NO MOCKEAR REPOSITORIOS
        // Los mocks se configurarán en cada test específicamente

        // ✅ Usuario propietario
        owner = EntityModelFactory
                .restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);

        // ✅ Cuisine
        cuisine= EntityModelFactory
                .restaurantCuisine(CUISINE_ID, "Mediterránea");

        // ✅ Restaurante
        restaurant1 = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

        // ✅ Restaurante 1
        restaurant1.setCuisine(cuisine);

        // ✅ Restaurante 2
        restaurant2 = EntityModelFactory.restaurant(RESTAURANT_ID_2, owner);
        restaurant2.setCuisine(cuisine);

        // ✅ Categorías
        category1 = EntityModelFactory.defaultCategory();
        category2 = EntityModelFactory.pastasCategory();

        restaurant1.setOfferedCategories(new HashSet<>(Set.of(category1, category2)));
    }

    /**
     * Método auxiliar para mockear SecurityContext
     * Úsalo en cada test que lo necesite
     */
//    private void setupSecurityContextWithEmail(String email) {
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getName()).thenReturn(email);
//
//        SecurityContext securityContext = mock(SecurityContext.class);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        SecurityContextHolder.setContext(securityContext);
//    }

    // ==================== FIND ALL TESTS ====================

    @Nested
    @DisplayName("findAll() Tests")
    class FindAllTests {

        /**
         * Test: Lista vacía de restaurantes
         *
         * Verificación:
         * ✅ repositoryRepository.findAll() llamado
         * ✅ Retorna Collections.emptyList()
         */
        @Test
        @DisplayName("Sin restaurantes → retorna lista vacía")
        void whenNoRestaurants_thenReturnsEmptyList() {
            // Arrange
            when(restaurantRepository.findAll())
                    .thenReturn(Collections.emptyList());

            // Act
            List<RestaurantResponseDto> result = restaurantService.findAll();

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertTrue(result.isEmpty(), "Lista debe estar vacía");
            assertEquals(0, result.size(), "Tamaño debe ser 0");

            // Verify
            verify(restaurantRepository, times(1)).findAll();
        }

        /**
         * Test: Lista con restaurantes
         *
         * Verificación:
         * ✅ Restaurantes encontrados
         * ✅ DTOs mapeados correctamente
         * ✅ Manejo de nulls (cuisine, userEntity)
         */
        @Test
        @DisplayName("Con restaurantes → retorna lista con DTOs")
        void whenRestaurantsExist_thenReturnsList() {
            // Arrange
            when(restaurantRepository.findAll())
                    .thenReturn(List.of(restaurant1, restaurant2));

            // Act
            List<RestaurantResponseDto> result = restaurantService.findAll();

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(2, result.size(), "Debe haber 2 restaurantes");

            RestaurantResponseDto dto1 = result.get(0);
            assertEquals(RESTAURANT_ID, dto1.rst_id(), "ID debe coincidir");
            assertEquals("Atlántico", dto1.name(), "Nombre debe coincidir");
            assertEquals(OWNER_ID, dto1.rst_user_id(), "User ID debe coincidir");
            assertEquals(CUISINE_ID, dto1.cuisineId(), "Cuisine ID debe coincidir");

            // Verify
            verify(restaurantRepository, times(1)).findAll();
        }

        /**
         * Test: Restaurantes con valores null
         *
         * Verificación:
         * ✅ Manejo de cuisine null
         * ✅ Manejo de userEntity null
         */
        @Test
        @DisplayName("Restaurantes con campos null → mapea correctamente")
        void whenRestaurantHasNullFields_thenHandlesNulls() {
            // Arrange
            Restaurant restaurantWithNulls = new Restaurant();
            restaurantWithNulls.setId(1L);
            restaurantWithNulls.setName("Test");
            restaurantWithNulls.setDescription("Test desc");
            restaurantWithNulls.setUserEntity(null);  // null
            restaurantWithNulls.setCuisine(null);  // null

            when(restaurantRepository.findAll())
                    .thenReturn(List.of(restaurantWithNulls));

            // Act
            List<RestaurantResponseDto> result = restaurantService.findAll();

            // Assert
            assertEquals(1, result.size(), "Debe haber 1 restaurante");
            RestaurantResponseDto dto = result.get(0);
            assertNull(dto.rst_user_id(), "User ID puede ser null");
            assertNull(dto.cuisineId(), "Cuisine ID puede ser null");
            assertNull(dto.cuisineName(), "Cuisine name puede ser null");

            // Verify
            verify(restaurantRepository, times(1)).findAll();
        }
    }

    // ==================== FIND BY ID TESTS ====================

    @Nested
    @DisplayName("findById() Tests")
    class FindByIdTests {

        /**
         * Test: Restaurante encontrado exitosamente
         *
         * Verificación:
         * ✅ ID válido
         * ✅ Restaurante encontrado
         * ✅ Retorna DTO correcto
         */
        @Test
        @DisplayName("ID válido y existente → retorna RestaurantResponseDto")
        void whenValidId_thenReturnsRestaurant() {
            // Arrange
            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant1));

            // Act
            RestaurantResponseDto result = restaurantService.findById(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(RESTAURANT_ID, result.rst_id(), "ID debe coincidir");
            assertEquals("Atlántico", result.name(), "Nombre debe coincidir");
            assertEquals(OWNER_ID, result.rst_user_id(), "User ID debe coincidir");

            // Verify
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
        }

        /**
         * Test: ID no encontrado
         *
         * Verificación:
         * ✅ Lanza RestaurantNotFoundException
         */
        @Test
        @DisplayName("ID inexistente → lanza RestaurantNotFoundException")
        void whenIdNotFound_thenThrowsNotFoundException() {
            // Arrange
            when(restaurantRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            RestaurantNotFoundException exception = assertThrows(
                    RestaurantNotFoundException.class,
                    () -> restaurantService.findById(999L),
                    "Debe lanzar RestaurantNotFoundException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no se encontró"),
                    "Mensaje debe mencionar 'no se encontró'");

            // Verify
            verify(restaurantRepository, times(1)).findById(999L);
            verify(cuisineRepository, never()).findById(any());
            verify(restaurantRepository, never()).save(any());
        }
        /**
         * Test: ID null
         *
         * Verificación:
         * ✅ Lanza RestaurantNotFoundException por validación
         */
        @Test
        @DisplayName("ID null → lanza RestaurantNotFoundException")
        void whenIdNull_thenThrowsNotFoundException() {
            // Act & Assert
            RestaurantNotFoundException exception = assertThrows(
                    RestaurantNotFoundException.class,
                    () -> restaurantService.findById(null),
                    "Debe lanzar RestaurantNotFoundException"
            );

            // Verify (no se debe llamar al repository)
            verify(restaurantRepository, never()).findById(any());
        }

        /**
         * Test: ID <= 0
         *
         * Verificación:
         * ✅ Lanza RestaurantNotFoundException por validación
         */
        @Test
        @DisplayName("ID <= 0 → lanza RestaurantNotFoundException")
        void whenIdInvalid_thenThrowsNotFoundException() {
            // Act & Assert
            assertThrows(
                    RestaurantNotFoundException.class,
                    () -> restaurantService.findById(0L)
            );

            assertThrows(
                    RestaurantNotFoundException.class,
                    () -> restaurantService.findById(-1L)
            );

            // Verify
            verify(restaurantRepository, never()).findById(any());
        }
    }

    // ==================== FIND BY OWNER ID TESTS ====================

    @Nested
    @DisplayName("findRestaurantsByOwnerId() Tests")
    class FindByOwnerIdTests {
        /**
         * Método auxiliar para mockear SecurityContext
         * Se usa en cada test con diferentes emails
         *
         * @param email Email del usuario autenticado
         */
        private void setupSecurityContextWithEmail(String email) {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(email);

            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);
        }
        /**
         * Test: Restaurantes encontrados para owner autorizado
         *
         * Verificación:
         * ✅ Owner encontrado
         * ✅ Usuario autenticado es el propietario
         * ✅ Retorna lista de restaurantes
         */
        @Test
        @DisplayName("Owner autorizado con restaurantes → retorna lista")
        void whenAuthorizedOwner_thenReturnsList() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));

            RestaurantResponseDto dto1 = RestaurantFactory.responseFromRequest(
                    RestaurantFactory.defaultRequest(OWNER_ID, OWNER_EMAIL), RESTAURANT_ID
            );
            RestaurantResponseDto dto2 = RestaurantFactory.responseFromRequest(
                    RestaurantFactory.defaultRequest(OWNER_ID, OWNER_EMAIL), RESTAURANT_ID_2
            );

            when(restaurantRepository.findRestaurantsByOwnerId(OWNER_ID))
                    .thenReturn(List.of(dto1, dto2));

            // Act
            List<RestaurantResponseDto> result = restaurantService.findRestaurantsByOwnerId(OWNER_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(2, result.size(), "Debe haber 2 restaurantes");

            // Verify
            verify(userRepository, times(1)).findById(OWNER_ID);
            verify(restaurantRepository, times(1)).findRestaurantsByOwnerId(OWNER_ID);
        }

        /**
         * Test: Owner sin restaurantes
         *
         * Verificación:
         * ✅ Owner existe y es autorizado
         * ✅ Pero no tiene restaurantes
         * ✅ Retorna lista vacía
         */
        @Test
        @DisplayName("Owner sin restaurantes → retorna lista vacía")
        void whenOwnerHasNoRestaurants_thenReturnsEmptyList() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));

            when(restaurantRepository.findRestaurantsByOwnerId(OWNER_ID))
                    .thenReturn(Collections.emptyList());

            // Act
            List<RestaurantResponseDto> result = restaurantService.findRestaurantsByOwnerId(OWNER_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertTrue(result.isEmpty(), "Lista debe estar vacía");

            // Verify
            verify(restaurantRepository, times(1)).findRestaurantsByOwnerId(OWNER_ID);
        }

        /**
         * Test: Owner no encontrado
         *
         * Verificación:
         * ✅ Lanza UserNotFoundException
         */
        @Test
        @DisplayName("Owner ID no existe → lanza UserNotFoundException")
        void whenOwnerNotFound_thenThrowsUserNotFoundException() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);
            when(userRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> restaurantService.findRestaurantsByOwnerId(999L),
                    "Debe lanzar UserNotFoundException"
            );

            // Verify
            verify(restaurantRepository, never()).findRestaurantsByOwnerId(any());
        }

        /**
         * Test: Usuario no autorizado (diferente al propietario)
         *
         * Verificación:
         * ✅ Owner existe
         * ✅ Usuario autenticado es DIFERENTE
         * ✅ Lanza UnauthorizedAccessException
         */
        @Test
        @DisplayName("Usuario no es propietario → lanza UnauthorizedAccessException")
        void whenUserNotOwner_thenThrowsUnauthorizedException() {
            // Arrange
            setupSecurityContextWithEmail(UNAUTHORIZED_EMAIL);

            when(userRepository.findById(OWNER_ID))
                    .thenReturn(Optional.of(owner));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> restaurantService.findRestaurantsByOwnerId(OWNER_ID),
                    "Debe lanzar UnauthorizedAccessException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("permiso"),
                    "Mensaje debe mencionar permiso");

            // Verify
            verify(restaurantRepository, never()).findRestaurantsByOwnerId(any());
        }
    }

    // ==================== FIND BY ID FETCHING CATEGORIES TESTS ====================

    @Nested
    @DisplayName("findByIdFetchingCategories() Tests")
    class FindByIdFetchingCategoriesTests {

        /**
         * Test: Restaurante con categorías
         *
         * Verificación:
         * ✅ Restaurante encontrado
         * ✅ Categorías cargadas
         * ✅ Retorna Set<CategoryResponseDto>
         */
        @Test
        @DisplayName("Restaurante con categorías → retorna Set")
        void whenRestaurantHasCategories_thenReturnsSet() {
            // Arrange
            when(restaurantRepository.findByIdFetchingCategories(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant1));

            // Act
            Set<CategoryResponseDto> result = restaurantService.findByIdFetchingCategories(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(2, result.size(), "Debe haber 2 categorías");
            assertTrue(result.stream().anyMatch(c -> "Pizzas".equals(c.name())),
                    "Debe contener Pizzas");
            assertTrue(result.stream().anyMatch(c -> "Pastas".equals(c.name())),
                    "Debe contener Pastas");

            // Verify
            verify(restaurantRepository, times(1)).findByIdFetchingCategories(RESTAURANT_ID);
        }

        /**
         * Test: Restaurante sin categorías
         *
         * Verificación:
         * ✅ Restaurante existe
         * ✅ Sin categorías ofrecidas
         * ✅ Retorna Set vacío
         */
        @Test
        @DisplayName("Restaurante sin categorías → retorna Set vacío")
        void whenRestaurantHasNoCategories_thenReturnsEmptySet() {
            // Arrange
            Restaurant restaurantWithoutCategories = new Restaurant();
            restaurantWithoutCategories.setId(RESTAURANT_ID);
            restaurantWithoutCategories.setName("Sin Categorías");
            restaurantWithoutCategories.setOfferedCategories(new HashSet<>());  // Vacío

            when(restaurantRepository.findByIdFetchingCategories(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurantWithoutCategories));

            // Act
            Set<CategoryResponseDto> result = restaurantService.findByIdFetchingCategories(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertTrue(result.isEmpty(), "Set debe estar vacío");

            // Verify
            verify(restaurantRepository, times(1)).findByIdFetchingCategories(RESTAURANT_ID);
        }

        /**
         * Test: Restaurante no encontrado
         *
         * Verificación:
         * ✅ Lanza RestaurantNotFoundException
         */
        @Test
        @DisplayName("Restaurante no existe → lanza RestaurantNotFoundException")
        void whenRestaurantNotFound_thenThrowsNotFoundException() {
            // Arrange
            when(restaurantRepository.findByIdFetchingCategories(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            RestaurantNotFoundException exception = assertThrows(
                    RestaurantNotFoundException.class,
                    () -> restaurantService.findByIdFetchingCategories(999L),
                    "Debe lanzar RestaurantNotFoundException"
            );

            // Verify
            verify(restaurantRepository, times(1)).findByIdFetchingCategories(999L);
        }

        /**
         * Test: Retorna Set, no List
         *
         * Verificación:
         * ✅ El tipo de retorno es Set
         * ✅ No es List ni Collection
         */
        @Test
        @DisplayName("Retorna Set (no List)")
        void whenFetching_thenReturnsSet() {
            // Arrange
            when(restaurantRepository.findByIdFetchingCategories(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant1));

            // Act
            Set<CategoryResponseDto> result = restaurantService.findByIdFetchingCategories(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Resultado debe ser Set");
            assertTrue(result instanceof Set, "Debe ser instancia de Set");
            assertFalse(result instanceof List, "No debe ser List");

            // Verify
            verify(restaurantRepository, times(1)).findByIdFetchingCategories(RESTAURANT_ID);
        }
    }
}