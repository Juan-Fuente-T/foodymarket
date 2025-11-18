package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.dtos.request.AddReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.request.UpdateReviewDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ReviewResponseDto;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.Review;
import com.c24_39_t_webapp.restaurants.services.IReviewService;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {

    private final IReviewService iReviewService;

    /**
     *Endpoint add a new {@link Review} entity to the Db.
     *It's processed in the service {@link IReviewService#addReview(AddReviewDto, Long)}
     * @param reviewDto request {Long restaurantId, Integer score(in a 0 - 10 range), and a String Comment}.
     * @param  userDetails The user details of the authenticated user adding the review.
     *                     From here, we extract the user ID to associate with the
     * new {@link Review} and then it's requested in the service to validate they have a User role.
     * @return a Response Entity, which returns Http status 201(Created) when the task were finished.
     */

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Transactional
    public ResponseEntity<?> addReview(@RequestBody @Valid final AddReviewDto reviewDto,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserEntity().getId();
        ReviewResponseDto reviewResponseDto = iReviewService.addReview(reviewDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewResponseDto);
    }

    /**
     * EndPoint retrieves a list of {@link Review} by {@link Restaurant}.
     * it is processed in the service {@link IReviewService#getAllRestaurantReviews(Long)}
     * @param restaurantId The restaurant's id to retrieve reviews of itself.
     * @return a 200 ok, with a list of {@link ReviewResponseDto}
     */

    @GetMapping("/restaurant/{restaurantId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> allRestaurantReviews(@PathVariable final Long restaurantId) {
        List<ReviewResponseDto> reviewList = iReviewService.getAllRestaurantReviews(restaurantId);
        return ResponseEntity.ok(reviewList);
    }

    /**
     * EndPoint retrieves a {@link Review} looked for id.
     * it is processed in the service {@link IReviewService#getReviewById(Long)}
     * @param reviewId The Review id wanted.
     * @return Returns Code 200 ok, with {@link ReviewResponseDto}
     */
    @GetMapping("/id/{reviewId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getReviewById(@PathVariable final Long reviewId) {
        ReviewResponseDto reviewResponseDto = iReviewService.getReviewById(reviewId);
        return ResponseEntity.ok(reviewResponseDto);
    }

    /**
     * EndPoint updates a {@link Review} looked for id.
     * it is processed in the service {@link IReviewService#updateReview(UpdateReviewDto, Long)}
     * @param updateReviewDto The information needed to update a Review
     * which Contains (Long reviewToUpdateId, Integer score, String
     * comments)
     * @param reviewId Needed to confirm the same person who created the Review is the oly one who can update it.
     * @return A Response entity code 200.OK with a {@link ReviewResponseDto}
     * with the updated entity.
     */

    @PatchMapping()
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> updateReview(@RequestBody final UpdateReviewDto updateReviewDto,
                 @AuthenticationPrincipal Long reviewId) {
        ReviewResponseDto reviewResponseDto = iReviewService.updateReview(updateReviewDto, reviewId);
        return ResponseEntity.ok(reviewResponseDto);
    }

    /**
     * EndPoint deletes a {@link Review} looked for id.
     * it is processed in the service {@link IReviewService#deleteReview(Long, Long)}
     * @param reviewId The wanted id to delete.
     * @param userDetails The user details of the authenticated user deleting the review
     * @return an Empty responseEntity code 200.Ok if the review is deleted.
     */
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> deleteReview(@PathVariable final Long reviewId,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUserEntity().getId();
        iReviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok().build();
    }
}
