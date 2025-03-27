package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.request.AddReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.request.UpdateReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.exception.user_implementations.ResourceNotFoundException;
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
    public ReviewResponseDto addReview(AddReviewDto reviewDto, UserDetailsImpl userDetails) {

        log.info("verificando permisos para agregar una reseña");
        UserEntity user =
                userRepository.findById(userDetails.getId()).orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario buscado"));

        Restaurant restaurant = restaurantRepository.findById(reviewDto.restaurantId()).orElseThrow(() -> new ResourceNotFoundException(
                "No se encontró el restaurante buscado"));

        log.info("Creando la entidad al asignar Valores");
        Review review = new Review();
        review.setComments(reviewDto.comments());
        review.setScore(reviewDto.score());
        review.setRestaurant(restaurant);
        review.setUserEntity(user);

        reviewRepository.save(review);
        log.info("Reseña creada con exito!");
        return new ReviewResponseDto(review);
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
                        review.getId(),
                        review.getRestaurant(),
                        review.getUserEntity(),
                        review.getScore(),
                        review.getComments(),
                        review.getCreatedAt())
        ).collect(Collectors.toList());
    }

    @Override
    public ReviewResponseDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Reseña no " +
                        "encontrada!"));
        return new ReviewResponseDto(review);
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @Override
    public ReviewResponseDto updateReview(UpdateReviewDto updateReviewDto, UserDetailsImpl userDetails) {
        log.info("Buscando recursos para actualizar la reseña");
        Review review =
                reviewRepository.findById(updateReviewDto.reviewToUpdateId()).orElseThrow(() -> new ResourceNotFoundException(
                        "Reseña no encontrada"));

        log.info("validando que el usuario que esta intentando actualizar la reseña tenga los permisos necesarios id:" +
                        " {}",
                userDetails.getId());
        validateUserPermissions(userDetails, review.getUserEntity().getId());

        log.info("Actualizando datos");
        Optional.ofNullable(updateReviewDto.comments())
                .ifPresent(review::setComments);
        Optional.ofNullable(updateReviewDto.score())
                .ifPresent(review::setScore);

        reviewRepository.save(review);

        log.info("Reseña actualizada");
        return new ReviewResponseDto(review);
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @Override
    public void deleteReview(Long id, UserDetailsImpl userDetails) {
        log.info("Obteniendo recursos de la db");
        Review review = reviewRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("No se encontro la reseña"));

        log.info("Validando que el usuario tenga permisos para eliminar la reseña");
        validateUserPermissions(userDetails, review.getUserEntity().getId());

        log.warn("Eliminando la reseña");
        reviewRepository.deleteById(id);

        log.info("Reseña eliminada");
    }

    // Methods from a private usage

    private static void validateUserPermissions(UserDetailsImpl user, Long reviewId) {
        if (!reviewId.equals(user.getId())) {
            throw new UnauthorizedAccessException("El usuario no tiene permisos para el cambio");
        }
    }


}
