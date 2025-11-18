package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.UpdateReviewDto;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para el endpoint PATCH /api/review de ReviewController.
 *
 * Verifica que al actualizar una reseña, solo el usuario CLIENTE que la creó pueda hacerlo.
 * El controlador toma el userId desde @AuthenticationPrincipal y lo valida contra la reseña.
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
@DisplayName("ReviewController - PATCH /api/review (Update Review)")
public class ReviewControllerUpdateTests {

    private static final String REVIEW_ENDPOINT = "/api/review";
    private static final String CLIENT_EMAIL = "cliente@example.com";
    private static final long USER_ID = 1L;
    private static final long REVIEW_ID = 1L;
    private static final long RESTAURANT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - PATCH /api/review")
    class SuccessCases {

        private UpdateReviewDto validUpdateReviewDto;
        private ReviewResponseDto expectedReviewResponse;

        @BeforeEach
        void setUp() {
            validUpdateReviewDto = ReviewFactory.defaultUpdateReviewRequest(REVIEW_ID);
            expectedReviewResponse = ReviewFactory.responseFromUpdateRequest(validUpdateReviewDto, RESTAURANT_ID, USER_ID, "Juan Pérez");
        }

        /**
         * Test que verifica que al actualizar una reseña propia con datos válidos,
         * se retorna 200 OK con los datos actualizados.
         *
         * Arrange: Mock retorna la reseña actualizada
         * Act & Assert: Petición PATCH con autenticación, rol CLIENTE y CSRF, verifica 200
         * Verify: Servicio fue llamado con userId y dto correcto
         *
         * @throws Exception
         */
        @Test
        @DisplayName("PATCH /api/review/{id} - Retorna 200 OK al actualizar reseña propia válida")
        void whenUpdateOwnReviewWithValidData_thenReturnsOkWithUpdatedReview() throws Exception {
            log.info("Test: Actualizar reseña propia con datos válidos");

            // Arrange
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            when(reviewService.updateReview(any(UpdateReviewDto.class), eq(REVIEW_ID), eq(USER_ID)))
                    .thenReturn(expectedReviewResponse);

            // Act & Assert
            mockMvc.perform(patch(REVIEW_ENDPOINT + "/" + REVIEW_ID )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateReviewDto))
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.score").value(9))
                    .andExpect(jsonPath("$.comments").exists());

            // Verify
            verify(reviewService, times(1)).updateReview(any(UpdateReviewDto.class), eq(REVIEW_ID), eq(USER_ID));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - PATCH /api/review/{reviewId}")
    class ErrorCases {

        private UpdateReviewDto validUpdateReviewDto;

        @BeforeEach
        void setUp() {
            validUpdateReviewDto = ReviewFactory.defaultUpdateReviewRequest(REVIEW_ID);
        }

        /**
         * Test que verifica que si la reseña no existe, se retorna 404 Not Found.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/review/{id} - Retorna 404 si la reseña no existe")
        void whenReviewNotFound_thenReturns404() throws Exception {
            // Arrange
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            when(reviewService.updateReview(any(UpdateReviewDto.class), eq(REVIEW_ID), eq(USER_ID)))
                    .thenThrow(new ResourceNotFoundException("Reseña no encontrada"));

            // Act & Assert
            mockMvc.perform(patch(REVIEW_ENDPOINT + "/" + REVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateReviewDto))
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("ResourceNotFoundException"));

            // Verify
            verify(reviewService, times(1)).updateReview(any(UpdateReviewDto.class), eq(REVIEW_ID), eq(USER_ID));
        }

        /**
         * Test que verifica que si intenta actualizar una reseña de otro usuario, se retorna 403 Forbidden.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/review/{id} - Retorna 403 si intenta actualizar reseña de otro usuario")
        void whenTryingToUpdateOtherUserReview_thenReturnsForbidden() throws Exception {
            // Arrange
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            when(reviewService.updateReview(any(UpdateReviewDto.class), eq(REVIEW_ID), eq(USER_ID)))
                    .thenThrow(new UnauthorizedAccessException("El usuario no tiene permisos para el cambio"));

            // Act & Assert
            mockMvc.perform(patch(REVIEW_ENDPOINT + "/" + REVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateReviewDto))
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));

            // Verify
            verify(reviewService, times(1)).updateReview(any(UpdateReviewDto.class), eq(REVIEW_ID), eq(USER_ID));
        }

        /**
         * Test que verifica que sin autenticación, se retorna 401 Unauthorized.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/review/{id} - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert
            mockMvc.perform(patch(REVIEW_ENDPOINT + "/" + REVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateReviewDto))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Verify
            verify(reviewService, never()).updateReview(any(), any(), any());
        }

        /**
         * Test que verifica que sin rol CLIENTE, se retorna 403 Forbidden.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail PATCH /api/review/{id} - Retorna 403 sin rol CLIENTE")
        void whenNoClientRole_thenReturnsForbidden() throws Exception {
            // Arrange
            doThrow(new UnauthorizedAccessException("El usuario no tiene permisos para el cambio"))
                    .when(reviewService).updateReview(eq(validUpdateReviewDto), eq(REVIEW_ID), eq(USER_ID));

            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("RESTAURANTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            // Act & Assert
            mockMvc.perform(patch(REVIEW_ENDPOINT + "/" + REVIEW_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUpdateReviewDto))
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            // Verify
            verify(reviewService, times(1)).updateReview(eq(validUpdateReviewDto), eq(REVIEW_ID), eq(USER_ID));
        }
    }
}