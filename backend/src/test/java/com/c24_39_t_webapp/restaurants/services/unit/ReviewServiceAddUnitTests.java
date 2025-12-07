package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.request.ReviewRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.factories.ReviewFactory;
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
 * Test unitarios para ReviewService.addReview()
 *
 * ✅ Reviews, Users, Restaurants desde EntityModelFactory
 * ✅ DTOs desde ReviewFactory
 * ✅ Happy path + error cases + validation cases
 *
 * Cobertura:
 * ✅ Crear reseña exitosamente
 * ✅ Usuario no encontrado → Lanza excepción
 * ✅ Restaurante no encontrado → Lanza excepción
 * ✅ Datos inválidos → Lanza excepción
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("ReviewService - addReview()")
class ReviewServiceAddUnitTests {

    private static final Long USER_ID = 1L;
    private static final Long RESTAURANT_ID = 1L;
    private static final String USER_EMAIL = "user@test.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.ReviewServiceImpl reviewService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - addReview()")
    class SuccessCases {

        private UserEntity user;
        private Restaurant restaurant;
        private Review savedReview;
        private ReviewRequestDto validRequest;

        @BeforeEach
        void setUp() {
            // ✅ Usuario desde factory
            user = EntityModelFactory.clientEntity(USER_ID, USER_EMAIL);

            // ✅ Owner para restaurante
            UserEntity owner = EntityModelFactory.restaurantOwnerEntity(2L, "owner@test.com");

            // ✅ Restaurante desde factory
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);

            // ✅ Request válido desde ReviewFactory
            validRequest = ReviewFactory.defaultAddReviewRequest();

            // ✅ Review guardada desde factory (si existe)
            savedReview = EntityModelFactory.review(1L, user, restaurant, 8, "Excelente comida");
        }

        /**
         * Test: Crear reseña exitosamente
         *
         * Verificación:
         * ✅ UserRepository.findById() se llamó
         * ✅ RestaurantRepository.findById() se llamó
         * ✅ ReviewRepository.save() se llamó
         * ✅ Retorna ReviewResponseDto correcto
         */
        @Test
        @DisplayName("Crear reseña válida → Exitoso")
        void whenAddingValidReview_thenCreatesSuccessfully() {
            // Arrange
            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(user));
            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));
            when(reviewRepository.save(any(Review.class)))
                    .thenReturn(savedReview);

            // Act
            ReviewResponseDto result = reviewService.addReview(validRequest, USER_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(RESTAURANT_ID, result.restaurantId(), "Restaurant ID debe coincidir");
            assertEquals(USER_ID, result.userId(), "User ID debe coincidir");

            // Verify
            verify(userRepository, times(1)).findById(USER_ID);
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(reviewRepository, times(1)).save(any(Review.class));
        }

        /**
         * Test: Score y comentarios se guardan correctamente
         *
         * Verificación:
         * ✅ Score se mapea correctamente
         * ✅ Comments se mapean correctamente
         */
        @Test
        @DisplayName("Mapeo de datos → Correcto")
        void whenMappingReviewData_thenDataIsCorrect() {
            // Arrange
            ReviewRequestDto customRequest = ReviewFactory.addReviewRequestWith(
                    RESTAURANT_ID,
                    9,
                    "Muy buena experiencia"
            );

            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(user));
            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));
            when(reviewRepository.save(any(Review.class)))
                    .thenReturn(savedReview);

            // Act
            ReviewResponseDto result = reviewService.addReview(customRequest, USER_ID);

            // Assert
            assertNotNull(result);
            assertEquals(9, result.score(), "Score debe coincidir");

            // Verify
            verify(reviewRepository, times(1)).save(any(Review.class));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - addReview()")
    class ErrorCases {

        /**
         * Test: Usuario no encontrado
         *
         * Verificación:
         * ✅ Lanza ResourceNotFoundException
         * ✅ No busca restaurante
         */
        @Test
        @DisplayName("Usuario no encontrado → Lanza ResourceNotFoundException")
        void whenUserNotFound_thenThrowsResourceNotFoundException() {
            // Arrange
            ReviewRequestDto validRequest = ReviewFactory.defaultAddReviewRequest();

            when(userRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> reviewService.addReview(validRequest, 999L),
                    "Debe lanzar UserNotFoundException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("no se encontró"),
                    "Esperaba 'usuario'. Mensaje real: " + exception.getMessage()
            );

            // Verify
            verify(restaurantRepository, never()).findById(any());
        }

        /**
         * Test: Restaurante no encontrado
         *
         * Verificación:
         * ✅ Lanza ResourceNotFoundException
         * ✅ Usuario sí fue encontrado
         */
        @Test
        @DisplayName("Restaurante no encontrado → Lanza ResourceNotFoundException")
        void whenRestaurantNotFound_thenThrowsResourceNotFoundException() {
            // Arrange
            UserEntity user = EntityModelFactory.clientEntity(USER_ID, USER_EMAIL);
            ReviewRequestDto invalidRequest = ReviewFactory.addReviewRequestWith(999L, 8, "Comentario");

            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(user));
            when(restaurantRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            RestaurantNotFoundException exception = assertThrows(
                    RestaurantNotFoundException.class,
                    () -> reviewService.addReview(invalidRequest, USER_ID),
                    "Debe lanzar ResourceNotFoundException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("restaurante"),
                    "Esperaba 'restaurante'. Mensaje real: " + exception.getMessage()
            );

            // Verify
            verify(reviewRepository, never()).save(any());
        }
    }

    // ==================== VALIDATION TESTS ====================

    @Nested
    @DisplayName("Validation - addReview()")
    class ValidationTests {

        /**
         * Test: Score fuera de rango (< 0)
         *
         * Verificación:
         * ✅ Score debe estar entre 0 y 10
         */
        @Test
        @DisplayName("Score < 0 → Lanza excepción")
        void whenScoreLessThanZero_thenThrowsException() {
            // Arrange
            ReviewRequestDto invalidRequest = ReviewFactory.addReviewRequestWith(
                    RESTAURANT_ID,
                    -1,  // Invalid score
                    "Comentario"
            );

            UserEntity user = EntityModelFactory.clientEntity(USER_ID, USER_EMAIL);
            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(user));

            // Act & Assert
            assertThrows(Exception.class,
                    () -> reviewService.addReview(invalidRequest, USER_ID)
            );
        }

        /**
         * Test: Score fuera de rango (> 10)
         *
         * Verificación:
         * ✅ Score debe estar entre 0 y 10
         */
        @Test
        @DisplayName("Score > 10 → Lanza excepción")
        void whenScoreGreaterThanTen_thenThrowsException() {
            // Arrange
            ReviewRequestDto invalidRequest = ReviewFactory.addReviewRequestWith(
                    RESTAURANT_ID,
                    11,  // Invalid score
                    "Comentario"
            );

            UserEntity user = EntityModelFactory.clientEntity(USER_ID, USER_EMAIL);
            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(user));

            // Act & Assert
            assertThrows(Exception.class,
                    () -> reviewService.addReview(invalidRequest, USER_ID)
            );
        }

        /**
         * Test: Comentarios vacíos
         *
         * Verificación:
         * ✅ Comments no puede estar vacío
         */
        @Test
        @DisplayName("Comentarios vacíos → Lanza excepción")
        void whenCommentsEmpty_thenThrowsException() {
            // Arrange
            ReviewRequestDto invalidRequest = ReviewFactory.addReviewRequestWith(
                    RESTAURANT_ID,
                    8,
                    ""  // Empty comments
            );

            UserEntity user = EntityModelFactory.clientEntity(USER_ID, USER_EMAIL);
            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(user));

            // Act & Assert
            assertThrows(Exception.class,
                    () -> reviewService.addReview(invalidRequest, USER_ID)
            );
        }
    }
}
