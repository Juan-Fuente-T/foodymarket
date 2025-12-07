package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;
import com.c24_39_t_webapp.restaurants.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.*;

/**
 * Test unitarios para ReviewService.getReviewById()
 *
 * ✅ Reviews desde EntityModelFactory
 * ✅ Happy path + error cases
 *
 * Cobertura:
 * ✅ Obtener reseña por ID válido
 * ✅ Reseña no encontrada → Lanza excepción
 * ✅ ID inválido (null, <= 0) → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("ReviewService - getReviewById()")
class ReviewServiceGetByIdUnitTests {

    private static final Long REVIEW_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long RESTAURANT_ID = 1L;

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
    @DisplayName("Success Cases - getReviewById()")
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
            review = EntityModelFactory.review(REVIEW_ID, user, restaurant, 8, "Excelente comida");
        }

        /**
         * Test: Obtener reseña por ID válido
         *
         * Verificación:
         * ✅ ReviewRepository.findById() se llamó
         * ✅ Retorna ReviewResponseDto correcto
         */
        @Test
        @DisplayName("Reseña existe → Retorna datos correctos")
        void whenReviewExists_thenReturnsReview() {
            // Arrange
            when(reviewRepository.findById(REVIEW_ID))
                    .thenReturn(Optional.of(review));

            // Act
            ReviewResponseDto result = reviewService.getReviewById(REVIEW_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(RESTAURANT_ID, result.restaurantId(), "Restaurant ID debe coincidir");
            assertEquals(USER_ID, result.userId(), "User ID debe coincidir");
            assertEquals(8, result.score(), "Score debe coincidir");

            // Verify
            verify(reviewRepository, times(1)).findById(REVIEW_ID);
        }

        /**
         * Test: Mapeo de datos correcto
         *
         * Verificación:
         * ✅ Score se mapea correctamente
         * ✅ Comments se mapean correctamente
         */
        @Test
        @DisplayName("Mapeo de datos → Correcto")
        void whenMappingReviewData_thenDataIsCorrect() {
            // Arrange
            review = EntityModelFactory.review(REVIEW_ID, user, restaurant, 9, "Muy buena experiencia");

            when(reviewRepository.findById(REVIEW_ID))
                    .thenReturn(Optional.of(review));

            // Act
            ReviewResponseDto result = reviewService.getReviewById(REVIEW_ID);

            // Assert
            assertEquals(9, result.score(), "Score debe coincidir");
            assertEquals("Muy buena experiencia", result.comments(), "Comments debe coincidir");

            // Verify
            verify(reviewRepository, times(1)).findById(REVIEW_ID);
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - getReviewById()")
    class ErrorCases {

        /**
         * Test: Reseña no encontrada
         *
         * Verificación:
         * ✅ Lanza ResourceNotFoundException
         * ✅ Mensaje contiene "no encontrada"
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
                    () -> reviewService.getReviewById(999L),
                    "Debe lanzar ResourceNotFoundException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("reseña") ||
                            exception.getMessage().toLowerCase().contains("encontrada"),
                    "Esperaba 'reseña' o 'encontrada'. Mensaje real: " + exception.getMessage()
            );

            // Verify
            verify(reviewRepository, times(1)).findById(999L);
        }

        /**
         * Test: ID null
         *
         * Verificación:
         * ✅ Lanza excepción
         */
        @Test
        @DisplayName("ID null → Lanza excepción")
        void whenIdIsNull_thenThrowsException() {
            // Arrange
            when(reviewRepository.findById(null))
                    .thenThrow(new IllegalArgumentException("ID no puede ser null"));

            // Act & Assert
            assertThrows(Exception.class,
                    () -> reviewService.getReviewById(null)
            );

            // Verify
            verify(reviewRepository, times(1)).findById(null);
        }

        /**
         * Test: ID <= 0
         *
         * Verificación:
         * ✅ Lanza excepción
         */
        @Test
        @DisplayName("ID <= 0 → Lanza excepción")
        void whenIdIsInvalid_thenThrowsException() {
            // Arrange
            when(reviewRepository.findById(-1L))
                    .thenThrow(new IllegalArgumentException("ID no puede ser null"));

            // Act & Assert
            assertThrows(Exception.class,
                    () -> reviewService.getReviewById(-1L)
            );

            // Verify
            verify(reviewRepository, times(1)).findById(-1L);
        }
    }
}