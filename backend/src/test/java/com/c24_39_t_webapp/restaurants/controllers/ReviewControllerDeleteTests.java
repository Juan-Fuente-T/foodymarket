package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.exception.ResourceNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.services.IReviewService;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test para el endpoint DELETE /api/review de ReviewController.
 *
 * Verifica que al eliminar una reseña, solo el usuario CLIENTE que la creó pueda hacerlo.
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
@DisplayName("ReviewController - DELETE /api/review (Delete Review)")
public class ReviewControllerDeleteTests {

    private static final String REVIEW_ENDPOINT = "/api/review";
    private static final String CLIENT_EMAIL = "cliente@example.com";
    private static final long USER_ID = 1L;
    private static final long REVIEW_ID = 1L;
    private static final long RESTAURANT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReviewService reviewService;

    // ==================== SUCCESS CASES ====================

    @Nested
    @DisplayName("Success Cases - DELETE /api/review")
    class SuccessCases {

        /**
         * Test que verifica que al eliminar una reseña propia, se retorna 200 OK.
         *
         * Arrange: Mock configura deleteReview para no hacer nada (void)
         * Act & Assert: Petición DELETE con autenticación, rol CLIENTE y CSRF, verifica 200
         * Verify: Servicio fue llamado una sola vez
         *
         * @throws Exception
         */
        @Test
        @DisplayName("DELETE /api/review - Retorna 200 OK al eliminar reseña propia")
        void whenDeleteOwnReview_thenReturnsOk() throws Exception {
            log.info("Test: Eliminar reseña propia");
            // Arrange
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            doNothing().when(reviewService).deleteReview(eq(USER_ID), eq(REVIEW_ID));

            // Act & Assert
            mockMvc.perform(delete(REVIEW_ENDPOINT + "/" + REVIEW_ID )
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
//                            .with(user(CLIENT_EMAIL).roles("CLIENTE"))
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk());
            // Verify
            verify(reviewService, times(1)).deleteReview(eq(USER_ID), eq(REVIEW_ID));
        }
    }

    // ==================== ERROR CASES ====================

    @Nested
    @DisplayName("Error Cases - DELETE /api/review")
    class ErrorCases {

        /**
         * Test que verifica que si la reseña no existe, se retorna 404 Not Found.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/review - Retorna 404 si la reseña no existe")
        void whenReviewNotFound_thenReturns404() throws Exception {
            // Arrange
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            doThrow(new ResourceNotFoundException("No se encontro la reseña"))
                    .when(reviewService).deleteReview(eq(USER_ID), eq(REVIEW_ID));

            // Act & Assert
            mockMvc.perform(delete(REVIEW_ENDPOINT + "/" + REVIEW_ID )
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("ResourceNotFoundException"));
            // Verify
            verify(reviewService, times(1)).deleteReview(eq(USER_ID), eq(REVIEW_ID));
        }

        /**
         * Test que verifica que si intenta eliminar una reseña de otro usuario, se retorna 403 Forbidden.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/review - Retorna 403 si intenta eliminar reseña de otro usuario")
        void whenTryingToDeleteOtherUserReview_thenReturnsForbidden() throws Exception {
            // Arrange
            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("CLIENTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            doThrow(new UnauthorizedAccessException("El usuario no tiene permisos para el cambio"))
                    .when(reviewService).deleteReview(eq(USER_ID), eq(REVIEW_ID));

            // Act & Assert
            mockMvc.perform(delete(REVIEW_ENDPOINT + "/" + REVIEW_ID )
                            .with(authentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("UnauthorizedAccessException"));
            // Verify
            verify(reviewService, times(1)).deleteReview(eq(USER_ID), eq(REVIEW_ID));
        }

        /**
         * Test que verifica que sin autenticación, se retorna 401 Unauthorized.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/review - Retorna 401 sin autenticación")
        void whenNoAuthentication_thenReturns401() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(REVIEW_ENDPOINT + "/" + REVIEW_ID )
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
            // Verify
            verify(reviewService, never()).deleteReview(any(), any());
        }

        /**
         * Test que verifica que sin rol CLIENTE, se retorna 403 Forbidden.
         *
         * @throws Exception
         */
        @Test
        @DisplayName("Fail DELETE /api/review - Retorna 403 sin rol CLIENTE")
        void whenNoClientRole_thenReturnsForbidden() throws Exception {
            // Arrange - Crea un UserEntity, un UserDetailsImpl válido y un mock de la respuesta del servicio
            doThrow(new UnauthorizedAccessException("El usuario no tiene permisos para el cambio"))
                    .when(reviewService).deleteReview(eq(REVIEW_ID), eq(USER_ID));

            UserEntity userEntity = new UserEntity();
            userEntity.setId(USER_ID);
            userEntity.setEmail(CLIENT_EMAIL);
            userEntity.setRole("RESTAURANTE");

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            // Act & Assert
            mockMvc.perform(delete(REVIEW_ENDPOINT + "/" + REVIEW_ID )
                            .with(authentication(new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            )))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
            // Verify
            verify(reviewService, times(1)).deleteReview(eq(REVIEW_ID), eq(USER_ID));
        }
    }
}
