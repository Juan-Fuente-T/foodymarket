package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.AddReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;
import com.c24_39_t_webapp.restaurants.exception.ResourceNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.factories.ReviewFactory;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.services.IReviewService;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para el endpoint POST /api/review de ReviewController.
 *
 * Verifica que al crear una reseña, solo un usuario CLIENTE autenticado pueda hacerlo.
 * El controlador toma el userId desde @AuthenticationPrincipal.
 *
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
@DisplayName("ReviewController - POST /api/review (Create Review)")
public class ReviewControllerCreateTests {

    private static final String REVIEW_ENDPOINT = "/api/review";
    private static final String CLIENT_EMAIL = "cliente@example.com";
    private static final long USER_ID = 1L;
    private static final long RESTAURANT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - POST /api/review")
    class SuccessCases {

        private AddReviewDto validAddReviewDto;
        private ReviewResponseDto expectedReviewResponse;

        @BeforeEach
        void setUp() {
            validAddReviewDto = ReviewFactory.addReviewRequestWith(RESTAURANT_ID, 8, "Excelente comida y servicio");
            expectedReviewResponse = ReviewFactory.responseFromAddRequest(validAddReviewDto, 1L, USER_ID, "Juan Pérez");
        }

        /**
         * Test que verifica que al crear una reseña con datos válidos,
         * se retorna 201 Created con los datos de la reseña creada.
         *
         * El controlador toma userId desde @AuthenticationPrincipal y lo pasa al servicio.
         *
         * Arrange: Mock retorna la reseña creada
         * Act & Assert: Petición POST con autenticación, rol CLIENTE y CSRF, verifica 201
         * Verify: Servicio fue llamado con userId y dto correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("POST /api/review - Retorna 201 Created al crear reseña válida")
        void whenCreateReviewWithValidData_thenReturnsCreatedWithReviewData() throws Exception {
            log.info("Test: Crear reseña con datos válidos");

            // Arrange
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);


            when(reviewService.addReview(any(AddReviewDto.class), eq(USER_ID)))
                    .thenReturn(expectedReviewResponse);

            // Act & Assert
            mockMvc.perform(post(REVIEW_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAddReviewDto))
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.restaurantId").value(RESTAURANT_ID))
                    .andExpect(jsonPath("$.userId").value(USER_ID))
                    .andExpect(jsonPath("$.score").value(8));

            // Verify
            verify(reviewService, times(1)).addReview(any(AddReviewDto.class), eq(USER_ID));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - POST /api/review")
    class ErrorCases {

        private AddReviewDto validAddReviewDto;

        @BeforeEach
        void setUp() {
            validAddReviewDto = ReviewFactory.defaultAddReviewRequest();
        }

        /**
         * Test que verifica que si el restaurante no existe, se retorna 404 Not Found.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail POST /api/review - Retorna 404 si el restaurante no existe")
        void whenRestaurantNotFound_thenReturns404() throws Exception {
            // Arrange
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            when(reviewService.addReview(any(AddReviewDto.class), eq(USER_ID)))
                    .thenThrow(new ResourceNotFoundException("No se encontró el restaurante buscado"));

            // Act & Assert
            mockMvc.perform(post(REVIEW_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAddReviewDto))
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("ResourceNotFoundException"));

            // Verify
            verify(reviewService, times(1)).addReview(any(AddReviewDto.class), eq(USER_ID));
        }

        /**
         * Test que verifica que sin autenticación, se retorna 401 Unauthorized.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail POST /api/review - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert - SIN autenticación
            mockMvc.perform(post(REVIEW_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAddReviewDto))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify
            verify(reviewService, never()).addReview(any(), any());
        }

        /**
         * Test que verifica que sin rol CLIENTE, se retorna 403 Forbidden.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail POST /api/review - Retorna 403 sin rol CLIENTE")
        void whenNoClientRole_thenReturnsForbidden() throws Exception {
            // Arrange - Crea un UserEntity, un UserDetailsImpl válido y un mock de la respuesta del servicio
            when(reviewService.addReview(any(AddReviewDto.class), any(Long.class)))
                    .thenThrow(new UnauthorizedAccessException("No tienes permiso para hacer esto"));

            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("RESTAURANTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            // Act & Assert
            mockMvc.perform(post(REVIEW_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAddReviewDto))
                            .with(authentication(new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            )))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));

            verify(reviewService, times(1)).addReview(any(AddReviewDto.class), any(Long.class));
        }

        /**
         * Test que verifica validación de score fuera del rango 0-10.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail POST /api/review - Retorna 400 si score está fuera del rango 0-10")
        void whenScoreOutOfRange_thenReturnsBadRequest() throws Exception {
            // Arrange - Score fuera del rango
            AddReviewDto invalidDto = ReviewFactory.addReviewRequestWith(RESTAURANT_ID, 15, "Comentario");

            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            // Act & Assert
            mockMvc.perform(post(REVIEW_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto))
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validación fallida"));

            // Verify
            verify(reviewService, never()).addReview(any(), any());
        }
    }
}