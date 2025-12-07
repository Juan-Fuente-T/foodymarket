package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.exception.ResourceNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.Review;
import com.c24_39_t_webapp.restaurants.repository.RestaurantRepository;
import com.c24_39_t_webapp.restaurants.repository.ReviewRepository;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para ReviewService.deleteReview()
 *
 * ✅ Reviews desde EntityModelFactory
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Eliminar reseña exitosamente
 * ✅ Reseña no encontrada → Lanza excepción
 * ✅ Usuario no autorizado → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("ReviewService - deleteReview()")
class ReviewServiceDeleteUnitTests {

    private static final Long REVIEW_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long RESTAURANT_ID = 1L;
    private static final Long OTHER_USER_ID = 999L;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.ReviewServiceImpl reviewService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - deleteReview()")
    class SuccessCases {

        private Review review;
        private UserEntity user;
        private Restaurant restaurant;

        @BeforeEach
        void setUp() {
            // ✅ Usuario desde factory
            user = EntityModelFactory.clientEntity(USER_ID, "user@test.com");

            // ✅ Owner para restaurante
            UserEntity owner = EntityModelFactory.restaurantOwnerEntity(2L, "owner@test.com");

            // ✅ Restaurante desde factory
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

            // ✅ Review desde factory
            review = EntityModelFactory.review(REVIEW_ID, user, restaurant, 8, "Excelente");
        }

        /**
         * Test: Eliminar reseña exitosamente
         *
         * Verificación:
         * ✅ ReviewRepository.findById() se llamó
         * ✅ ReviewRepository.deleteById() se llamó
         * ✅ NO lanza excepción
         */
        @Test
        @DisplayName("Eliminar reseña válida → Exitoso")
        void whenDeletingValidReview_thenDeletesSuccessfully() {
            // Arrange
            when(reviewRepository.findById(REVIEW_ID))
                    .thenReturn(Optional.of(review));

            // Act
            assertDoesNotThrow(() -> reviewService.deleteReview(REVIEW_ID, USER_ID));

            // Verify
            verify(reviewRepository, times(1)).findById(REVIEW_ID);
            verify(reviewRepository, times(1)).deleteById(REVIEW_ID);
        }

        /**
         * Test: Delete permite al usuario dueño eliminar su reseña
         *
         * Verificación:
         * ✅ El usuario que crea la reseña puede eliminarla
         */
        @Test
        @DisplayName("Usuario dueño elimina reseña → Exitoso")
        void whenOwnerDeletesOwnReview_thenDeletesSuccessfully() {
            // Arrange
            review.setId(USER_ID);  // ← Review ID = User ID para validación

            when(reviewRepository.findById(REVIEW_ID))
                    .thenReturn(Optional.of(review));

            // Act
            assertDoesNotThrow(() -> reviewService.deleteReview(REVIEW_ID, USER_ID));

            // Verify
            verify(reviewRepository, times(1)).deleteById(REVIEW_ID);
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - deleteReview()")
    class ErrorCases {

        private Review review;
        private UserEntity user;
        private Restaurant restaurant;

        @BeforeEach
        void setUp() {
            user = EntityModelFactory.clientEntity(USER_ID, "user@test.com");
            UserEntity owner = EntityModelFactory.restaurantOwnerEntity(2L, "owner@test.com");
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);
            review = EntityModelFactory.review(REVIEW_ID, user, restaurant, 8, "Excelente");
        }

        /**
         * Test: Reseña no encontrada
         *
         * Verificación:
         * ✅ Lanza ResourceNotFoundException
         * ✅ No intenta eliminar
         */
        @Test
        @DisplayName("Reseña no encontrada → Lanza ResourceNotFoundException")
        void whenReviewNotFound_thenThrowsResourceNotFoundException() {
            // Arrange
            when(reviewRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> reviewService.deleteReview(999L, USER_ID),
                    "Debe lanzar ResourceNotFoundException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("reseña"),
                    "Esperaba 'reseña'. Mensaje real: " + exception.getMessage()
            );

            // Verify
            verify(reviewRepository, never()).deleteById(any());
        }

        /**
         * Test: Usuario no autorizado
         *
         * Verificación:
         * ✅ Lanza UnauthorizedAccessException
         * ✅ Usuario que intenta eliminar no es dueño
         */
        @Test
        @DisplayName("Usuario no autorizado → Lanza UnauthorizedAccessException")
        void whenUserNotOwner_thenThrowsUnauthorizedException() {
            // Arrange
            when(reviewRepository.findById(REVIEW_ID))
                    .thenReturn(Optional.of(review));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> reviewService.deleteReview(REVIEW_ID, OTHER_USER_ID),
                    "Debe lanzar UnauthorizedAccessException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("permiso"),
                    "Esperaba 'permiso'. Mensaje real: " + exception.getMessage()
            );

            // Verify
            verify(reviewRepository, never()).deleteById(any());
        }
    }
}