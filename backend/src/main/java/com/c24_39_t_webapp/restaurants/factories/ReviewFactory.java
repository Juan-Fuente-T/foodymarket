package com.c24_39_t_webapp.restaurants.factories;

import com.c24_39_t_webapp.restaurants.dtos.request.AddReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.request.UpdateReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory para generar datos de prueba para Review DTOs.
 * Sigue el mismo patrón que ProductFactory, OrderFactory, UserFactory.
 *
 * Cada método retorna NUEVAS instancias para evitar contaminación entre tests.
 */
public final class ReviewFactory {

    private ReviewFactory() {
    }

    // ================= REQUEST payload =================

    /**
     * Payload DEFAULT para crear una reseña (POST /api/review).
     * Cada llamada retorna una NUEVA instancia.
     *
     * @return nueva instancia de AddReviewDto con datos por defecto
     */
    public static AddReviewDto defaultAddReviewRequest() {
        return new AddReviewDto(
                1L,          // restaurantId
                8,           // score (0-10)
                "Excelente comida y buen servicio"  // comments
        );
    }

    /**
     * Payload para crear una reseña con datos personalizados.
     *
     * @param restaurantId ID del restaurante
     * @param score        Puntuación (0-10)
     * @param comments     Comentario
     * @return nueva instancia de AddReviewDto personalizado
     */
    public static AddReviewDto addReviewRequestWith(Long restaurantId, Integer score, String comments) {
        return new AddReviewDto(restaurantId, score, comments);
    }

    /**
     * Payload DEFAULT para actualizar una reseña (PATCH /api/review).
     * Cada llamada retorna una NUEVA instancia.
     *
     * @param reviewId ID de la reseña a actualizar
     * @return nueva instancia de UpdateReviewDto con datos por defecto
     */
    public static UpdateReviewDto defaultUpdateReviewRequest(Long reviewId) {
        return new UpdateReviewDto(
                reviewId,    // reviewToUpdateId
                9,           // score actualizado
                "Excelente experiencia, volvería con gusto"  // comments actualizado
        );
    }

    /**
     * Payload para actualizar una reseña con datos personalizados.
     *
     * @param reviewId ID de la reseña
     * @param score    Puntuación actualizada
     * @param comments Comentario actualizado
     * @return nueva instancia de UpdateReviewDto personalizado
     */
    public static UpdateReviewDto updateReviewRequestWith(Long reviewId, Integer score, String comments) {
        return new UpdateReviewDto(reviewId, score, comments);
    }

    // ================= RESPONSE payload =================

    /**
     * Payload DEFAULT para la respuesta al crear o actualizar una reseña.
     * Usado en POST /api/review, PATCH /api/review y GET endpoints.
     * Cada llamada retorna una NUEVA instancia.
     *
     * @param reviewId ID de la reseña
     * @return nueva instancia de ReviewResponseDto con datos por defecto
     */
    public static ReviewResponseDto defaultReviewResponse(Long reviewId) {
        LocalDateTime now = LocalDateTime.now();

        return new ReviewResponseDto(
                1L,          // restaurantId
                1L,          // userId
                "Juan Pérez",  // userName
                8,           // score
                "Excelente comida y buen servicio",  // comments
                now          // createdAt
        );
    }

    /**
     * Genera una Response coherente a partir de un AddReviewDto.
     *
     * @param req      Datos base de un AddReviewDto
     * @param reviewId ID de la reseña generada
     * @param userId   ID del usuario que creó la reseña
     * @param userName Nombre del usuario
     * @return nueva instancia de ReviewResponseDto mapeado desde el request
     */
    public static ReviewResponseDto responseFromAddRequest(AddReviewDto req, Long reviewId, Long userId, String userName) {
        LocalDateTime now = LocalDateTime.now();

        return new ReviewResponseDto(
                req.restaurantId(),
                userId,
                userName,
                req.score(),
                req.comments(),
                now
        );
    }

    /**
     * Genera una Response coherente a partir de un UpdateReviewDto.
     *
     * @param req      Datos base de un UpdateReviewDto
     * @param restaurantId ID del restaurante de la reseña
     * @param userId   ID del usuario que creó la reseña
     * @param userName Nombre del usuario
     * @return nueva instancia de ReviewResponseDto mapeado desde el request
     */
    public static ReviewResponseDto responseFromUpdateRequest(UpdateReviewDto req, Long restaurantId, Long userId, String userName) {
        LocalDateTime now = LocalDateTime.now();

        return new ReviewResponseDto(
                restaurantId,
                userId,
                userName,
                req.score(),
                req.comments(),
                now
        );
    }

    /**
     * Genera una lista de responses por defecto para tests que requieren múltiples reseñas.
     *
     * @return List con 3 ReviewResponseDto por defecto
     */
    public static List<ReviewResponseDto> responseListDefault() {
        List<ReviewResponseDto> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        list.add(new ReviewResponseDto(1L, 1L, "Juan Pérez", 8, "Excelente comida", now));
        list.add(new ReviewResponseDto(1L, 2L, "María García", 7, "Muy bueno", now));
        list.add(new ReviewResponseDto(1L, 3L, "Carlos López", 9, "Perfecto", now));

        return list;
    }
}