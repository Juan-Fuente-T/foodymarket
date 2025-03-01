package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.services.IRestaurantService;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/restaurant")
public class RestaurantController {
    @Autowired
    private IRestaurantService restaurantService;

    /**
     * Test endpoint to verify that the controller handles POST requests correctly.
     * Responds with a message including the received name parameter.
     *
     * @param name The name parameter received in the request body.
     * @return A string indicating that the controller works and including the received name.
     */
    @PostMapping(value = "/testPostMethod")
    public String testControllerPost(@RequestBody String name) {
        return "El metodo POST del controller de Restaurants funciona ok, " + name + "!";
    }

    /**
     * Another test endpoint demonstrating GET request handling.
     * Responds with a custom message.
     *
     * @return A string indicating that the GET method works.
     */
    @GetMapping(value = "/testMethod")
    public String testControllerGet() {
        return "El metodo GET del controller de Restaurants funciona ok!";
    }

    /**
     * Endpoint to register a restaurant.
     * Responds with the status for the new restaurant request.
     * @param restaurantRequestDto get the necessary information to create the request.
     * @param userDetails Request to the token the user details, avoiding to the user enter again their information.
     *                    (did not allow to the user enter any other wrong information)
     * @return a response entity with the new restaurant uri
     */

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('restaurante')")
    public ResponseEntity<?> registerRestaurant(@RequestBody @Valid final RestaurantRequestDto restaurantRequestDto,
                                                @AuthenticationPrincipal final UserDetailsImpl userDetails) {
        RestaurantResponseDto restaurant = restaurantService.
                registerRestaurant(restaurantRequestDto, userDetails.getUsername());

        URI uri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/restaurant/{rst_id}")
                .buildAndExpand(restaurant.getRst_id())
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    /**
     * Endpoint to retrieve a list of all {@link ResponseEntity} objects stored in the system.
     * Delegates the retrieval logic to {@link IRestaurantService#findAll()}.
     *
     * @return A list of {@code ContactDTO} objects representing all contacts.
     */
    @GetMapping("/all")
    public ResponseEntity<List<RestaurantResponseDto>> getAllRestaurants() {
            log.info("Solicitud recibida para obtener todos los restaurantes.");
            List<RestaurantResponseDto> restaurants = restaurantService.findAll();

            log.info("Se recuperaron {} restaurantes exitosamente.", restaurants.size());
            return ResponseEntity.ok(restaurants);
    }

    /**
     * Endpoint to query a single restaurant based on the provided ID.
     * Delegates the query logic to {@link IRestaurantService#findById(Long)}.
     *
     * @param rst_id The ID of the restaurant to query.
     * @return The {@code RestaurantResponseDto} object representing the queried restaurant.
     */
    @GetMapping("/{rst_id}")
    public ResponseEntity<RestaurantResponseDto> findRestaurantById(@PathVariable Long rst_id) {
            log.info("Solicitud recibida para obtener un restaurante usando un id.");
            RestaurantResponseDto restaurant = restaurantService.findById(rst_id);

            log.info("Se recuperaró el restaurante exitosamente.");
            return ResponseEntity.ok(restaurant);
    }

    /**
     * Endpoint to update an existing restaurant in the system using the provided {@link RestaurantRequestDto}.
     * Delegates the update logic to {@link IRestaurantService#updateRestaurant(Long, RestaurantRequestDto)}.
     *
     * @param rst_id The ID of the restaurant to update.
     * @param restaurantRequestDto The {@link RestaurantRequestDto} object containing the updated details of the restaurant.
     * @return The {@code RestaurantResponseDto} object representing the updated restaurant.
     */
    //    @PutMapping("/{rst_id}")
    @PatchMapping("/{rst_id}")
//    public int updateRestaurant(@RequestBody RestaurantResponseDto restaurantResponseDto) {
    public ResponseEntity<RestaurantResponseDto> updateRestaurant(
            @PathVariable Long rst_id,
            @RequestBody @Valid RestaurantRequestDto restaurantRequestDto
    ) {
            log.info("Solicitud recibida para actualizar el restaurante con ID: {}", rst_id);
            RestaurantResponseDto updatedRestaurant = restaurantService.updateRestaurant(rst_id, restaurantRequestDto);
            log.info("Restaurante con ID: {} actualizado exitosamente", rst_id);
            return ResponseEntity.ok(updatedRestaurant);
    }

    @DeleteMapping("/{rst_id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long rst_id) {
            log.info("Solicitud recibida para eliminar el restaurante con ID: {}", rst_id);
            restaurantService.deleteById(rst_id);
            log.info("Restaurante con ID: {} eliminado exitosamente", rst_id);
            return ResponseEntity.noContent().build();
    }
}