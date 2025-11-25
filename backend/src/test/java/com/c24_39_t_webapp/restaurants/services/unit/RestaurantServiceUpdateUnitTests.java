package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para RestaurantService.updateRestaurant()
 * <p>
 * ✅ TODOS los datos vienen de RestaurantFactory
 * ✅ Usuarios vienen de EntityModelFactory
 * ✅ Sincronización con factories
 * ✅ Happy path + error cases
 * <p>
 * Cobertura:
 * ✅ Actualización exitosa: datos válidos + usuario autorizado
 * ✅ Restaurante no encontrado
 * ✅ Cuisine no encontrada
 * ✅ Usuario no autorizado (no es propietario)
 * ✅ Verifica que restaurantRepository.save() se llamó
 * ✅ Retorna RestaurantResponseDto con datos actualizados
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("RestaurantService - updateRestaurant()")
public class RestaurantServiceUpdateUnitTests {

    private static final Long RESTAURANT_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long CUISINE_ID = 1L;
    private static final String OWNER_EMAIL = "owner@restaurant.com";
    private static final String UNAUTHORIZED_EMAIL = "other@restaurant.com";

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.RestaurantRepository restaurantRepository;

    @Mock
    private com.c24_39_t_webapp.restaurants.repository.RestaurantCuisineRepository cuisineRepository;


    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.RestaurantServiceImpl restaurantService;

    /**
     * Método auxiliar para mockear SecurityContext
     * Úsalo en cada test que lo necesite
     */
    private void setupSecurityContextWithEmail(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // ==================== CASOS DE ÉXITO ====================

    @Nested
    @DisplayName("Success Cases - updateRestaurant()")
    class SuccessCases {
        private RestaurantRequestDto validUpdateRequest;
        private Restaurant existingRestaurant;
        private UserEntity restaurantOwner;
        private RestaurantCuisine validCuisine;

        @BeforeEach
        void setUp() {
            // ✅ Datos desde RestaurantFactory (para el request)
            validUpdateRequest = RestaurantFactory.defaultRequest(USER_ID, OWNER_EMAIL);

            // ✅ Usuario propietario desde EntityModelFactory
            restaurantOwner = com.c24_39_t_webapp.restaurants.factories.EntityModelFactory
                    .restaurantOwnerEntity(USER_ID, OWNER_EMAIL);

            // ✅ Restaurante existente
            existingRestaurant = com.c24_39_t_webapp.restaurants.factories.EntityModelFactory
                    .restaurant(RESTAURANT_ID, restaurantOwner);

            validCuisine = EntityModelFactory.restaurantCuisine(CUISINE_ID, "Pizza");
            existingRestaurant.setCuisine(validCuisine);
//            log.info("Rest. CUISINE: " + existingRestaurant.getCuisine());
        }

        /**
         * Test: Actualización EXITOSA con datos válidos
         * <p>
         * Verificación:
         * ✅ Restaurante encontrado
         * ✅ Cuisine encontrada
         * ✅ Usuario autenticado es propietario
         * ✅ restaurantRepository.save() se llamó una vez
         * ✅ Retorna RestaurantResponseDto con datos actualizados
         */
        @Test
        @DisplayName("Datos válidos + usuario autorizado → Actualiza exitosamente")
        void whenValidDataAndAuthorized_thenUpdatesSuccessfully() {
            // Arrange - Configurar SecurityContext ESPECÍFICAMENTE para este test
            setupSecurityContextWithEmail(OWNER_EMAIL);

            RestaurantRequestDto updateNameRequest = RestaurantFactory.updatedRequestFrom(
                    validUpdateRequest,
                    "NOMBRE ACTUALIZADO",  // newName
                    null,                   // newPhone - no cambiar
                    null                    // newAddress - no cambiar
            );

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(existingRestaurant));

            when(cuisineRepository.findById(validUpdateRequest.cuisineId()))
                    .thenReturn(Optional.of(validCuisine));

            when(restaurantRepository.save(any(Restaurant.class)))
                    .thenReturn(existingRestaurant);

            RestaurantResponseDto result = restaurantService.updateRestaurant(
                    updateNameRequest, RESTAURANT_ID
            );

            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(RESTAURANT_ID, result.rst_id(), "ID debe coincidir");

            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(restaurantRepository, times(1)).save(any(Restaurant.class));
        }

        /**
         * Test: restaurantRepository.save() recibe entidad con cambios aplicados
         *
         * Verificación:
         * ✅ Captura el argumento pasado a save()
         * ✅ Verifica que TODOS los campos del request se actualizaron en la entidad
         */
        @Test
        @DisplayName("restaurantRepository.save() recibe Restaurant con datos actualizados")
        void whenSaving_thenRestaurantHasUpdatedData() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(existingRestaurant));

            when(cuisineRepository.findById(validUpdateRequest.cuisineId()))
                    .thenReturn(Optional.of(validCuisine));

            ArgumentCaptor<Restaurant> restaurantCaptor = ArgumentCaptor.forClass(Restaurant.class);
            when(restaurantRepository.save(restaurantCaptor.capture()))
                    .thenReturn(existingRestaurant);

            // Act
            restaurantService.updateRestaurant(validUpdateRequest, RESTAURANT_ID);

            // Assert
            Restaurant capturedRestaurant = restaurantCaptor.getValue();
            assertEquals(validUpdateRequest.name(), capturedRestaurant.getName(),
                    "Nombre debe estar actualizado");
            assertEquals(validUpdateRequest.description(), capturedRestaurant.getDescription(),
                    "Descripción debe estar actualizada");
            assertEquals(validUpdateRequest.phone(), capturedRestaurant.getPhone(),
                    "Teléfono debe estar actualizado");
            assertEquals(validUpdateRequest.email(), capturedRestaurant.getEmail(),
                    "Email debe estar actualizado");
            assertEquals(validUpdateRequest.address(), capturedRestaurant.getAddress(),
                    "Dirección debe estar actualizada");
            assertEquals(validUpdateRequest.openingHours(), capturedRestaurant.getOpeningHours(),
                    "Horario debe estar actualizado");
            assertEquals(validCuisine, capturedRestaurant.getCuisine(),
                    "Cuisine debe estar actualizado");
        }

        /**
         * Test: Actualizar algunos campos y otros permanecen iguales
         * <p>
         * Verificación:
         * ✅ La dirección cambia
         * ✅ El teléfono cambia
         * ✅ El nombre sigue siendo el mismo
         */
        @Test
        @DisplayName("Actualiza parcialmente: campos indicados cambian, otros permanecen")
        void whenPartialUpdate_thenOnlyRequestedFieldsChange() {
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);


            RestaurantRequestDto partialUpdate = RestaurantFactory.updatedRequestFrom(
                    validUpdateRequest,
                    null,                   // newName - no cambiar
                    "111222333",                   // newPhone
                    "NUEVA DIRECCIÓN"       // newAddress
            );

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(existingRestaurant));

            when(cuisineRepository.findById(validUpdateRequest.cuisineId()))
                    .thenReturn(Optional.of(validCuisine));

            when(restaurantRepository.save(any(Restaurant.class)))
                    .thenReturn(existingRestaurant);

            // Act
            RestaurantResponseDto result = restaurantService.updateRestaurant(
                    partialUpdate, RESTAURANT_ID
            );

            // Assert
            assertEquals(USER_ID, result.rst_user_id(), "User ID debe mantenerse igual");
            assertEquals("111222333", result.phone(), "Teléfono debe cambiar");
            assertEquals("NUEVA DIRECCIÓN", result.address(), "La dirección debe cambiar");
            assertEquals(validUpdateRequest.name(), result.name(), "User Name debe mantenerse igual");

            // Verify
            verify(restaurantRepository, times(1)).save(any(Restaurant.class));
        }
    }

    // ==================== CASOS DE ERROR ====================

    @Nested
    @DisplayName("Error Cases - updateRestaurant()")
    class ErrorCases {

        private RestaurantRequestDto validUpdateRequest;
        private Restaurant existingRestaurant;
        private UserEntity restaurantOwner;
        private RestaurantCuisine validCuisine;

        @BeforeEach
        void setUp() {
            // ✅ INICIALIZACIÓN COMPLETA - Independiente de la clase padre
            validUpdateRequest = RestaurantFactory.defaultRequest(USER_ID, OWNER_EMAIL);

            restaurantOwner = EntityModelFactory.restaurantOwnerEntity(USER_ID, OWNER_EMAIL);

            existingRestaurant = EntityModelFactory.restaurant(RESTAURANT_ID, restaurantOwner);

            validCuisine = EntityModelFactory.restaurantCuisine(CUISINE_ID, "Pizza");
            existingRestaurant.setCuisine(validCuisine);
        }

        /**
         * Test: Restaurante NO ENCONTRADO
         * <p>
         * Verificación:
         * ✅ Lanza RestaurantNotFoundException
         * ✅ No busca cuisine
         * ✅ No persiste nada
         */
        @Test
        @DisplayName("Restaurante ID inválido → lanza RestaurantNotFoundException")
        void whenRestaurantNotFound_thenThrowsNotFoundException() {
            // Arrange
            when(restaurantRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            RestaurantNotFoundException exception = assertThrows(
                    RestaurantNotFoundException.class,
                    () -> restaurantService.updateRestaurant(validUpdateRequest, 999L),
                    "Debe lanzar RestaurantNotFoundException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("no se encontró"),
                    "Mensaje debe mencionar que no se encontró. Mensaje: " + exception.getMessage());

            // Verify
            verify(restaurantRepository, times(1)).findById(999L);
            verify(restaurantRepository, never()).save(any());
        }

        /**
         * Test: Cuisine NO ENCONTRADA
         * <p>
         * Verificación:
         * ✅ Restaurante existe
         * ✅ Cuisine ID es inválido
         * ✅ Lanza ResponseStatusException con BAD_REQUEST
         * ✅ No guarda cambios
         */
        @Test
        @DisplayName("Cuisine ID inválido → lanza ResponseStatusException BAD_REQUEST")
        void whenCuisineNotFound_thenThrowsBadRequest() {
            // Arrange - Configurar ESPECÍFICAMENTE para este test
            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(existingRestaurant));

            // Act & Assert
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> restaurantService.updateRestaurant(validUpdateRequest, RESTAURANT_ID),
                    "Debe lanzar ResponseStatusException"
            );

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode(),
                    "Status debe ser BAD_REQUEST");

            // Verify
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(restaurantRepository, never()).save(any());
        }

        /**
         * Test: Usuario NO AUTORIZADO (no es propietario)
         * <p>
         * Verificación:
         * ✅ Restaurante existe
         * ✅ Cuisine existe
         * ✅ Usuario autenticado es DIFERENTE al propietario
         * ✅ Lanza UnauthorizedAccessException
         * ✅ No guarda cambios
         */
        @Test
        @DisplayName("Usuario no es propietario → lanza UnauthorizedAccessException")
        void whenUserNotOwner_thenThrowsUnauthorizedException() {
            // Arrange
            // Cambiar el email del usuario autenticado en SecurityContext
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn(UNAUTHORIZED_EMAIL);

            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(existingRestaurant));

            when(cuisineRepository.findById(validUpdateRequest.cuisineId()))
                    .thenReturn(Optional.of(validCuisine));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> restaurantService.updateRestaurant(validUpdateRequest, RESTAURANT_ID),
                    "Debe lanzar UnauthorizedAccessException"
            );

            assertTrue(exception.getMessage().toLowerCase().contains("permiso"),
                    "Mensaje debe mencionar permiso. Mensaje: " + exception.getMessage());

            // Verify
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(restaurantRepository, never()).save(any());
        }

        /**
         * Test: UserEntity null en restaurante
         * <p>
         * Verificación:
         * ✅ Restaurante existe pero sin usuario
         * ✅ Lanza UnauthorizedAccessException
         */
        @Test
        @DisplayName("Restaurante sin UserEntity → lanza UnauthorizedAccessException")
        void whenRestaurantHasNoUser_thenThrowsUnauthorizedException() {
//            log.info("Restaurant Cuisine ID: " + existingRestaurant.getCuisine().getId());
            // Arrange
            setupSecurityContextWithEmail(OWNER_EMAIL);
            existingRestaurant.setUserEntity(null);

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(existingRestaurant));

            when(cuisineRepository.findById(existingRestaurant.getCuisine().getId()))
                    .thenReturn(Optional.of(validCuisine));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> restaurantService.updateRestaurant(validUpdateRequest, RESTAURANT_ID)
            );

            // Verify
            verify(restaurantRepository, never()).save(any());
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases - updateRestaurant()")
    class EdgeCases {
        private Restaurant existingRestaurant;
        private UserEntity restaurantOwner;
        private RestaurantCuisine validCuisine;
        private RestaurantCuisine newCuisine;

        @BeforeEach
        void setUp() {
            // ✅ Usuario propietario
            restaurantOwner = EntityModelFactory.restaurantOwnerEntity(USER_ID, OWNER_EMAIL);

            // ✅ Restaurante existente
            existingRestaurant = EntityModelFactory.restaurant(RESTAURANT_ID, restaurantOwner);

            validCuisine = EntityModelFactory.restaurantCuisine(CUISINE_ID, "Pizza");
            existingRestaurant.setCuisine(validCuisine);  // ← Inicializado
            newCuisine = EntityModelFactory.restaurantCuisine(2L, "Italiana");
        }

            /**
             * Test: Actualizar TODOS los campos
             */
            @Test
            @DisplayName("Actualizar TODOS los campos → Se actualizan correctamente")
            void whenUpdateAllFields_thenAllFieldsAreUpdated() {
                setupSecurityContextWithEmail(OWNER_EMAIL);
                Long newCuisineID = newCuisine.getId();

                // ✅ Crear DTO que cambia TODOS los campos
                RestaurantRequestDto updateAllRequest = new RestaurantRequestDto(
                        USER_ID,
                        "NOMBRE NUEVO",
                        "DESCRIPCIÓN NUEVA",
                        newCuisineID,  // ← Nueva Cuisine
                        "999 999 999",
                        "newemail@restaurant.com",
                        "DIRECCIÓN NUEVA",
                        "09:00-23:00",
                        "https://example.com/logo_new.png",
                        "https://example.com/cover_new.jpg"
                );

                when(restaurantRepository.findById(RESTAURANT_ID))
                        .thenReturn(Optional.of(existingRestaurant));

                when(cuisineRepository.findById(newCuisineID))
                        .thenReturn(Optional.of(newCuisine));

                when(restaurantRepository.save(any(Restaurant.class)))
                        .thenReturn(existingRestaurant);

                // Act
                RestaurantResponseDto result = restaurantService.updateRestaurant(
                        updateAllRequest, RESTAURANT_ID
                );

                // Assert
                assertNotNull(result);
                assertEquals(RESTAURANT_ID, result.rst_id());
                assertEquals("NOMBRE NUEVO", result.name());
                assertEquals("DESCRIPCIÓN NUEVA", result.description());
                assertEquals("999 999 999", result.phone());
                assertEquals("newemail@restaurant.com", result.email());
                assertEquals("DIRECCIÓN NUEVA", result.address());

                // Verify
                verify(restaurantRepository, times(1)).save(any(Restaurant.class));
            }
        }

}
