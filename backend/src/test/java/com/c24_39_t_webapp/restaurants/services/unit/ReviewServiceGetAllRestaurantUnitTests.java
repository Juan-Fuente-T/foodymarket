package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;
import com.c24_39_t_webapp.restaurants.exception.ResourceNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para ReviewService.getAllRestaurantReviews()
 *
 * ✅ Restaurantes y Reviews desde EntityModelFactory
 * ✅ Happy path + edge cases + error cases
 *
 * Cobertura:
 * ✅ Obtener todas las reseñas de un restaurante
 * ✅ Lista vacía de reseñas
 * ✅ Restaurante no encontrado → Lanza excepción
 * ✅ Múltiples reseñas
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("ReviewService - getAllRestaurantReviews()")
class ReviewServiceGetAllRestaurantUnitTests {

    private static final Long RESTAURANT_ID = 1L;
    private static final Long OWNER_ID = 1L;
    private static final String OWNER_EMAIL = "owner@test.com";

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private com.c24_39_t_webapp.restaurants.services.impl.ReviewServiceImpl reviewService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - getAllRestaurantReviews()")
    class SuccessCases {

        private Restaurant restaurant;
        private UserEntity owner;

        @BeforeEach
        void setUp() {
            // ✅ Owner desde factory
            owner = EntityModelFactory.restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);

            // ✅ Restaurante desde factory
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);
        }

        /**
         * Test: Obtener todas las reseñas de un restaurante
         *
         * Verificación:
         * ✅ RestaurantRepository.findById() se llamó
         * ✅ ReviewRepository.findByRestaurant() se llamó
         * ✅ Retorna lista con reseñas
         */
        @Test
        @DisplayName("Obtener todas las reseñas → Retorna lista")
        void whenGettingAllReviews_thenReturnsList() {
            // Arrange
            UserEntity user1 = EntityModelFactory.clientEntity(1L, "user1@test.com");
            UserEntity user2 = EntityModelFactory.clientEntity(2L, "user2@test.com");

            Review review1 = EntityModelFactory.review(1L, user1, restaurant, 8, "Excelente");
            Review review2 = EntityModelFactory.review(2L, user2, restaurant, 7, "Muy bueno");

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));
            when(reviewRepository.findByRestaurant(restaurant))
                    .thenReturn(Arrays.asList(review1, review2));

            // Act
            List<ReviewResponseDto> result = reviewService.getAllRestaurantReviews(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(2, result.size(), "Debe retornar 2 reseñas");
            assertEquals(8, result.get(0).score(), "Primera reseña tiene score 8");
            assertEquals(7, result.get(1).score(), "Segunda reseña tiene score 7");

            // Verify
            verify(restaurantRepository, times(1)).findById(RESTAURANT_ID);
            verify(reviewRepository, times(1)).findByRestaurant(restaurant);
        }

        /**
         * Test: Una única reseña
         *
         * Verificación:
         * ✅ Retorna lista con 1 elemento
         */
        @Test
        @DisplayName("Una única reseña → Retorna lista con 1 elemento")
        void whenOnlyOneReview_thenReturnsListWithOneElement() {
            // Arrange
            UserEntity user = EntityModelFactory.clientEntity(1L, "user@test.com");
            Review review = EntityModelFactory.review(1L, user, restaurant, 9, "Perfecto");

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));
            when(reviewRepository.findByRestaurant(restaurant))
                    .thenReturn(Arrays.asList(review));

            // Act
            List<ReviewResponseDto> result = reviewService.getAllRestaurantReviews(RESTAURANT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size(), "Debe retornar 1 reseña");

            // Verify
            verify(reviewRepository, times(1)).findByRestaurant(restaurant);
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases - getAllRestaurantReviews()")
    class EdgeCases {

        private Restaurant restaurant;
        private UserEntity owner;

        @BeforeEach
        void setUp() {
            owner = EntityModelFactory.restaurantOwnerEntity(OWNER_ID, OWNER_EMAIL);
            restaurant = EntityModelFactory.restaurant(RESTAURANT_ID, owner);
        }

        /**
         * Test: Sin reseñas
         *
         * Verificación:
         * ✅ Retorna lista vacía
         * ✅ No es null
         */
        @Test
        @DisplayName("Sin reseñas → Retorna lista vacía")
        void whenNoReviews_thenReturnsEmptyList() {
            // Arrange
            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));
            when(reviewRepository.findByRestaurant(restaurant))
                    .thenReturn(new ArrayList<>());

            // Act
            List<ReviewResponseDto> result = reviewService.getAllRestaurantReviews(RESTAURANT_ID);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertTrue(result.isEmpty(), "Lista debe estar vacía");
            assertEquals(0, result.size());

            // Verify
            verify(reviewRepository, times(1)).findByRestaurant(restaurant);
        }

        /**
         * Test: Muchas reseñas
         *
         * Verificación:
         * ✅ Retorna lista completa
         */
        @Test
        @DisplayName("Muchas reseñas → Retorna lista completa")
        void whenManyReviews_thenReturnsCompleteList() {
            // Arrange
            List<Review> reviewList = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                UserEntity user = EntityModelFactory.clientEntity((long) i, "user" + i + "@test.com");
                Review review = EntityModelFactory.review((long) i, user, restaurant, 5 + i, "Review " + i);
                reviewList.add(review);
            }

            when(restaurantRepository.findById(RESTAURANT_ID))
                    .thenReturn(Optional.of(restaurant));
            when(reviewRepository.findByRestaurant(restaurant))
                    .thenReturn(reviewList);

            // Act
            List<ReviewResponseDto> result = reviewService.getAllRestaurantReviews(RESTAURANT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(5, result.size(), "Debe retornar 5 reseñas");

            // Verify
            verify(reviewRepository, times(1)).findByRestaurant(restaurant);
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - getAllRestaurantReviews()")
    class ErrorCases {

        /**
         * Test: Restaurante no encontrado
         *
         * Verificación:
         * ✅ Lanza ResourceNotFoundException
         * ✅ No llama a ReviewRepository
         */
        @Test
        @DisplayName("Restaurante no encontrado → Lanza ResourceNotFoundException")
        void whenRestaurantNotFound_thenThrowsResourceNotFoundException() {
            // Arrange
            when(restaurantRepository.findById(999L))
                    .thenReturn(Optional.empty());

            // Act & Assert
            RestaurantNotFoundException exception = assertThrows(
                    RestaurantNotFoundException.class,
                    () -> reviewService.getAllRestaurantReviews(999L),
                    "Debe lanzar ResourceNotFoundException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("restaurante"),
                    "Esperaba 'restaurante'. Mensaje real: " + exception.getMessage()
            );

            // Verify
            verify(reviewRepository, never()).findByRestaurant(any());
        }
    }
}