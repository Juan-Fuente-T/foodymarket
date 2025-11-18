package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;
import com.c24_39_t_webapp.restaurants.exception.ResourceNotFoundException;
import com.c24_39_t_webapp.restaurants.factories.ReviewFactory;
import com.c24_39_t_webapp.restaurants.services.IReviewService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para los endpoints GET /api/review de ReviewController.
 * <p>
 * Verifica que los endpoints GET (públicos sin @PreAuthorize) funcionen correctamente
 * para obtener reseñas de un restaurante o por ID.
 * <p>
 * Patrón AAA: Arrange, Act, Assert
 */
@WebMvcTest(
        controllers = ReviewController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@Slf4j
@DisplayName("ReviewController - GET /api/review (Get Reviews)")
public class ReviewControllerGetTests {

    private static final String REVIEW_ENDPOINT = "/api/review";
    private static final String RESTAURANT_REVIEWS_ENDPOINT = REVIEW_ENDPOINT + "/restaurant";
    private static final String REVIEW_BY_ID_ENDPOINT = REVIEW_ENDPOINT + "/id";
    private static final long RESTAURANT_ID = 1L;
    private static final long REVIEW_ID = 1L;
    private static final String CLIENT_EMAIL = "cliente@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReviewService reviewService;

    // ==================== SUCCESS CASES - GET /api/review/restaurant ====================

    @Nested
    @DisplayName("Success Cases - GET /api/review/restaurant")
    class GetAllRestaurantReviewsSuccessCases {

        private List<ReviewResponseDto> expectedReviewList;

        @BeforeEach
        void setUp() {
            expectedReviewList = ReviewFactory.responseListDefault();
        }

        /**
         * Test que verifica que al obtener todas las reseñas de un restaurante,
         * se retorna 200 OK con lista de reseñas.
         * <p>
         * Este endpoint es PÚBLICO (sin @PreAuthorize), no requiere autenticación.
         * <p>
         * Arrange: Mock retorna lista de reseñas
         * Act & Assert: Petición GET con restaurantId, verifica 200 OK
         * Verify: Servicio fue llamado con el restaurantId correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/review/restaurant - Retorna 200 OK con lista de reseñas del restaurante")
        void whenGetRestaurantReviews_thenReturnsOkWithReviewsList() throws Exception {
            log.info("Test: Obtener todas las reseñas de un restaurante");

            // Arrange
            when(reviewService.getAllRestaurantReviews(eq(RESTAURANT_ID)))
                    .thenReturn(expectedReviewList);

            // Act & Assert
//NO, solo para @RequestParam (parametros opcionales en la URL)
//            mockMvc.perform(get(RESTAURANT_REVIEWS_ENDPOINT)
//                            .param("restaurantId", String.valueOf(RESTAURANT_ID))
            mockMvc.perform(get(REVIEW_ENDPOINT + "/restaurant/" + RESTAURANT_ID)
                            .with(user(CLIENT_EMAIL).roles("CLIENTE")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3));

            // Verify
            verify(reviewService, times(1)).getAllRestaurantReviews(eq(RESTAURANT_ID));
        }

        /**
         * Test que verifica que una lista vacía retorna 200 OK con array vacío.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/review/restaurant - Retorna 200 OK con array vacío si no hay reseñas")
        void whenNoReviewsForRestaurant_thenReturnsOkWithEmptyList() throws Exception {
            // Arrange
            when(reviewService.getAllRestaurantReviews(eq(RESTAURANT_ID)))
                    .thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get(REVIEW_ENDPOINT + "/restaurant/" + RESTAURANT_ID)
                            .with(user(CLIENT_EMAIL).roles("CLIENTE")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            // Verify
            verify(reviewService, times(1)).getAllRestaurantReviews(eq(RESTAURANT_ID));
        }
    }

    // ==================== ERROR CASES - GET /api/review/restaurant ====================

    @Nested
    @DisplayName("Error Cases - GET /api/review/restaurant")
    class GetAllRestaurantReviewsErrorCases {

        /**
         * Test que verifica que si el restaurante no existe, se retorna 404 Not Found.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/review/restaurant - Retorna 404 si restaurante no existe")
        void whenRestaurantNotFound_thenReturns404() throws Exception {
            // Arrange
            when(reviewService.getAllRestaurantReviews(eq(RESTAURANT_ID)))
                    .thenThrow(new ResourceNotFoundException("El restaurante con id enviado no existe"));

            // Act & Assert
            mockMvc.perform(get(REVIEW_ENDPOINT + "/restaurant/" + RESTAURANT_ID)
                            .with(user(CLIENT_EMAIL).roles("CLIENTE")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("ResourceNotFoundException"));

            // Verify
            verify(reviewService, times(1)).getAllRestaurantReviews(eq(RESTAURANT_ID));
        }
    }

    // ==================== SUCCESS CASES - GET /api/review/id ====================

    @Nested
    @DisplayName("Success Cases - GET /api/review/id")
    class GetReviewByIdSuccessCases {

        private ReviewResponseDto expectedReview;

        @BeforeEach
        void setUp() {
            expectedReview = ReviewFactory.defaultReviewResponse(REVIEW_ID);
        }

        /**
         * Test que verifica que al obtener una reseña por ID,
         * se retorna 200 OK con los datos de la reseña.
         * <p>
         * Este endpoint es PÚBLICO (sin @PreAuthorize), no requiere autenticación.
         * <p>
         * Arrange: Mock retorna la reseña
         * Act & Assert: Petición GET con reviewId, verifica 200 OK
         * Verify: Servicio fue llamado con el reviewId correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("GET /api/review/id - Retorna 200 OK con datos de la reseña")
        void whenGetReviewById_thenReturnsOkWithReviewData() throws Exception {
            log.info("Test: Obtener reseña por ID");

            // Arrange
            when(reviewService.getReviewById(eq(REVIEW_ID)))
                    .thenReturn(expectedReview);

            // Act & Assert
            mockMvc.perform(get(REVIEW_ENDPOINT + "/id/" + REVIEW_ID)
                            .with(user(CLIENT_EMAIL).roles("CLIENTE")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.restaurantId").value(1L))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.score").value(8));

            // Verify
            verify(reviewService, times(1)).getReviewById(eq(REVIEW_ID));
        }
    }

    // ==================== ERROR CASES - GET /api/review/id ====================

    @Nested
    @DisplayName("Error Cases - GET /api/review/id")
    class GetReviewByIdErrorCases {

        /**
         * Test que verifica que si la reseña no existe, se retorna 404 Not Found.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail GET /api/review/id - Retorna 404 si la reseña no existe")
        void whenReviewNotFound_thenReturns404() throws Exception {
            // Arrange
            when(reviewService.getReviewById(eq(REVIEW_ID)))
                    .thenThrow(new ResourceNotFoundException("Reseña no encontrada!"));

            // Act & Assert
            mockMvc.perform(get(REVIEW_ENDPOINT + "/restaurant/" + RESTAURANT_ID)
                            .with(user(CLIENT_EMAIL).roles("CLIENTE")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("ResourceNotFoundException"));

            // Verify
            verify(reviewService, times(1)).getReviewById(eq(REVIEW_ID));
        }
    }
}