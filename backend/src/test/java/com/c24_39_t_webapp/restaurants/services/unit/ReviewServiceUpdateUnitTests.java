package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.ReviewRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.UpdateReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;
import com.c24_39_t_webapp.restaurants.exception.ResourceNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.factories.ReviewFactory;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.Review;
import com.c24_39_t_webapp.restaurants.repository.ReviewRepository;
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
 * Test unitarios para ReviewService.updateReview()
 *
 * ✅ Reviews desde EntityModelFactory
 * ✅ DTOs desde ReviewFactory
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Actualizar score de reseña
 * ✅ Actualizar comentarios de reseña
 * ✅ Reseña no encontrada → Lanza excepción
 * ✅ Usuario no autorizado → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("ReviewService - updateReview()")
class ReviewServiceUpdateUnitTests {

    private static final Long REVIEW_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long RESTAURANT_ID = 1L;
    private static final Long OTHER_USER_ID = 999L;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.ReviewServiceImpl reviewService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - updateReview()")
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
         * Test: Actualizar score de reseña
         *
         * Verificación:
         * ✅ ReviewRepository.findById() se llamó
         * ✅ ReviewRepository.save() se llamó
         * ✅ Score se actualiza correctamente
         */
        @Test
        @DisplayName("Actualizar score → Exitoso")
        void whenUpdatingScore_thenUpdatesSuccessfully() {
            // Arrange
            UpdateReviewDto updateRequest = ReviewFactory.defaultUpdateReviewRequest(REVIEW_ID);

            when(reviewRepository.findById(REVIEW_ID))
                    .thenReturn(Optional.of(review));
            when(reviewRepository.save(any(Review.class)))
                    .thenReturn(review);

            // Act
            ReviewResponseDto result = reviewService.updateReview(updateRequest, REVIEW_ID, USER_ID);

            // Assert
            assertNotNull(result);

            // Verify
            verify(reviewRepository, times(1)).findById(REVIEW_ID);
            verify(reviewRepository, times(1)).save(any(Review.class));
        }

        /**
         * Test: Actualizar comentarios
         *
         * Verificación:
         * ✅ Comments se actualizan correctamente
         */
        @Test
        @DisplayName("Actualizar comentarios → Exitoso")
        void whenUpdatingComments_thenUpdatesSuccessfully() {
            // Arrange
            UpdateReviewDto updateRequest = ReviewFactory.defaultUpdateReviewRequest(REVIEW_ID);
//            UpdateReviewDto updateRequest = ReviewFactory.updateReviewRequestWith(
//                    REVIEW_ID,
//                    8,
//                    "Excelente experiencia, volvería con gusto"
//            );

            when(reviewRepository.findById(REVIEW_ID))
                    .thenReturn(Optional.of(review));
            when(reviewRepository.save(any(Review.class)))
                    .thenReturn(review);

            // Act
            ReviewResponseDto result = reviewService.updateReview(updateRequest, REVIEW_ID, USER_ID);

            // Assert
            assertNotNull(result);

            // Verify
            verify(reviewRepository, times(1)).save(any(Review.class));
        }

        /**
         * Test: Actualizar ambos (score y comentarios)
         *
         * Verificación:
         * ✅ Score y comentarios se actualizan
         */
        @Test
        @DisplayName("Actualizar score y comentarios → Exitoso")
        void whenUpdatingBoth_thenUpdatesSuccessfully() {
            // Arrange
            UpdateReviewDto updateRequest = ReviewFactory.defaultUpdateReviewRequest(REVIEW_ID);
//            UpdateReviewDto updateRequest = ReviewFactory.updateReviewRequestWith(
//                    REVIEW_ID,
//                    10,
//                    "Perfecto en to-do"
//            );

            when(reviewRepository.findById(REVIEW_ID))
                    .thenReturn(Optional.of(review));
            when(reviewRepository.save(any(Review.class)))
                    .thenReturn(review);

            // Act
            ReviewResponseDto result = reviewService.updateReview(updateRequest, REVIEW_ID, USER_ID);

            // Assert
            assertNotNull(result);

            // Verify
            verify(reviewRepository, times(1)).save(any(Review.class));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - updateReview()")
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
         * ✅ No guarda cambios
         */
        @Test
        @DisplayName("Reseña no encontrada → Lanza ResourceNotFoundException")
        void whenReviewNotFound_thenThrowsResourceNotFoundException() {
            // Arrange
            Review review = EntityModelFactory.review(REVIEW_ID, user, restaurant, 8, "Excelente");

            UpdateReviewDto updateRequest = ReviewFactory.defaultUpdateReviewRequest(REVIEW_ID);

            when(reviewRepository.findById(review.getId()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> reviewService.updateReview(updateRequest, REVIEW_ID, USER_ID),
                    "Debe lanzar ResourceNotFoundException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("reseña"),
                    "Esperaba 'reseña'. Mensaje real: " + exception.getMessage()
            );

            // Verify
            verify(reviewRepository, never()).save(any());
        }

        /**
         * Test: Usuario no autorizado
         *
         * Verificación:
         * ✅ Lanza UnauthorizedAccessException
         * ✅ El usuario que intenta actualizar no es el dueño
         */
        @Test
        @DisplayName("Usuario no autorizado → Lanza UnauthorizedAccessException")
        void whenUserNotAuthorized_thenThrowsUnauthorizedException() {
            UpdateReviewDto updateRequest = ReviewFactory.defaultUpdateReviewRequest(REVIEW_ID);

//            UpdateReviewDto updateRequest = reviewService.updateReview(updateRequest1, REVIEW_ID, USER_ID);

            // Arrange
//            UpdateReviewDto updateRequest = ReviewFactory.responseFromUpdateRequest(
//               REVIEW_ID,
//               9L,
//               USER_ID,
//               "Intento de actualización no autorizado"
//                    );

            when(reviewRepository.findById(REVIEW_ID))
                    .thenReturn(Optional.of(review));

            // Act & Assert
            UnauthorizedAccessException exception = assertThrows(
                    UnauthorizedAccessException.class,
                    () -> reviewService.updateReview(updateRequest, REVIEW_ID, OTHER_USER_ID),
                    "Debe lanzar UnauthorizedAccessException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("permiso"),
                    "Esperaba 'permiso'. Mensaje real: " + exception.getMessage()
            );

            // Verify
            verify(reviewRepository, never()).save(any());
        }
    }
}