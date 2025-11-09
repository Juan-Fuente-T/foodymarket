package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.request.AddReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.request.UpdateReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.exception.ResourceNotFoundException;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.Review;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.RestaurantRepository;
import com.c24_39_t_webapp.restaurants.repository.ReviewRepository;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
import com.c24_39_t_webapp.restaurants.services.IReviewService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class ReviewServiceImpl implements IReviewService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;

    @PreAuthorize("hasRole('CLIENTE')")
    @Override
    public ReviewResponseDto addReview(AddReviewDto reviewDto, Long userId) {

        log.info("verificando permisos para agregar una reseña");
        UserEntity user =
                userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario buscado"));

        Restaurant restaurant = restaurantRepository.findById(reviewDto.restaurantId()).orElseThrow(() -> new ResourceNotFoundException(
                "No se encontró el restaurante buscado"));

        log.info("Creando la entidad al asignar Valores");
        Review review = new Review();
        review.setComments(reviewDto.comments());
        review.setScore(reviewDto.score());
        review.setRestaurant(restaurant);
        review.setUser(user);

        reviewRepository.save(review);
        log.info("Reseña creada con exito!");
        return new ReviewResponseDto(
                (restaurant != null) ? restaurant.getId() : null,
                (review.getRestaurant() != null) ? user.getId() : null,
                (user != null) ? user.getName() : null,
                review.getScore(),
                review.getComments(),
                review.getCreatedAt()
        );
    }

    @Override
    public List<ReviewResponseDto> getAllRestaurantReviews(Long restaurantId) {
        log.info("Obteniendo la Entidad para la busqueda de la Reseña, rstId: {}", restaurantId);
        Restaurant restaurant =
                restaurantRepository.findById(restaurantId).orElseThrow(() -> new ResourceNotFoundException("El " +
                        "restaurante con id enviado no existe"));

        log.info("Obteniendo la lista de reseñas del repositorio");
        List<Review> rewiewList = reviewRepository.findByRestaurant(restaurant);

        log.info("Convirtiendo las las reseñas en dto´s ");
        return rewiewList.stream().map(
                review -> new ReviewResponseDto(
                        review.getRestaurant().getId(),
                        review.getUser().getId(),
                        review.getUser().getName(),
                        review.getScore(),
                        review.getComments(),
                        review.getCreatedAt()
                )
        ).collect(Collectors.toList());
    }

    @Override
    public ReviewResponseDto getReviewById(Long reviewId) {
        log.info("Buscando la reseña con ID {}", reviewId);
        Review review = reviewRepository.findById(reviewId).orElseThrow(() ->
                new ResourceNotFoundException("Reseña no " +
                        "encontrada!"));
        Restaurant restaurant = review.getRestaurant();
        UserEntity user = review.getUser();

        return new ReviewResponseDto(
                (restaurant != null) ? restaurant.getId() : null,
                (user != null) ? user.getId() : null,
                (user != null) ? user.getName() : null,
                review.getScore(),
                review.getComments(),
                review.getCreatedAt()
        );
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @Override
    public ReviewResponseDto updateReview(UpdateReviewDto updateReviewDto, Long reviewId) {
        log.info("Buscando recursos para actualizar la reseña");
        Review review =
                reviewRepository.findById(updateReviewDto.reviewToUpdateId()).orElseThrow(() -> new ResourceNotFoundException(
                        "Reseña no encontrada"));

        log.info("validando que el usuario que esta intentando actualizar la reseña tenga los permisos necesarios id:" +
                        " {}", reviewId);
        validateUserPermissions(review, reviewId);
//        if (!review.getId().equals(userId)) {
//            throw new UnauthorizedAccessException("El usuario no tiene permisos para el cambio");
//        }

        log.info("Actualizando datos");
        Optional.ofNullable(updateReviewDto.comments())
                .ifPresent(review::setComments);
        Optional.ofNullable(updateReviewDto.score())
                .ifPresent(review::setScore);

        reviewRepository.save(review);

        log.info("Reseña actualizada");
        Restaurant restaurant = review.getRestaurant();
        UserEntity user = review.getUser();
        return new ReviewResponseDto(
                (restaurant != null) ? restaurant.getId() : null,
                (review.getRestaurant() != null) ? user.getId() : null,
                (user != null) ? user.getName() : null,
                review.getScore(),
                review.getComments(),
                review.getCreatedAt()
        );
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @Override
    public void deleteReview(Long reviewId) {
        log.info("Obteniendo recursos de la db");
        Review review = reviewRepository.findById(reviewId).orElseThrow(() ->
                new ResourceNotFoundException("No se encontro la reseña"));

        log.info("Validando que el usuario tenga permisos para eliminar la reseña");
        validateUserPermissions(review, reviewId);

        log.warn("Eliminando la reseña");
        reviewRepository.deleteById(reviewId);

        log.info("Reseña eliminada");
    }

    private static void validateUserPermissions(Review review, Long reviewId) {
        if (!review.getId().equals(reviewId)) {
            throw new UnauthorizedAccessException("El usuario no tiene permisos para el cambio");
        }
    }


}
