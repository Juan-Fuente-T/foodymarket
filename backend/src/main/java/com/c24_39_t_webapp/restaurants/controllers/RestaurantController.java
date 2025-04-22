package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.request.CategoryRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.services.IRestaurantService;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

//import java.net.URI;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/restaurant")
public class RestaurantController {
    @Autowired
    private IRestaurantService restaurantService;
    private RestaurantResponseDto restaurantResponseDto;

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

//    @PostMapping("/create")
    @PostMapping()
    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<?> registerRestaurant(@RequestBody @Valid final RestaurantRequestDto restaurantRequestDto,
                                                @AuthenticationPrincipal final UserDetailsImpl userDetails) {
        log.info("Solicitud recibida para registrar un restaurante para el usuario con email: {}", userDetails.getUsername());

        System.out.println("restaurantRequestDto = " + restaurantRequestDto);

        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantRequestDto.name());
        restaurant.setDescription(restaurantRequestDto.description());
        restaurant.setCategory(restaurantRequestDto.categoria());
        restaurant.setPhone(restaurantRequestDto.phone());
        restaurant.setEmail(restaurantRequestDto.email());
        restaurant.setAddress(restaurantRequestDto.address());
        restaurant.setOpeningHours(restaurantRequestDto.openingHours());
        restaurant.setLogo(restaurantRequestDto.logo());
        restaurant.setCoverImage(restaurantRequestDto.coverImage());

        RestaurantResponseDto restaurantResponseDto = restaurantService.
                registerRestaurant(restaurant, userDetails.getUsername());

//        URI uri = ServletUriComponentsBuilder
//                .fromCurrentContextPath()
//                .path("/api/restaurant/{rst_id}")
//                .buildAndExpand(restaurant.rst_id())
//                .toUri();
//
//        return ResponseEntity.created(uri).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantResponseDto);
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
     * Delegates the update logic to {@link IRestaurantService#updateRestaurant( Restaurant)}.
     *
     * @param rst_id The ID of the restaurant to update.
     * @param restaurantRequestDto The {@link RestaurantRequestDto} object containing the updated details of the restaurant.
     * @return The {@code RestaurantResponseDto} object representing the updated restaurant.
     */
    //    @PutMapping("/{rst_id}")
    @PatchMapping("/{rst_id}")
    @PreAuthorize("hasRole('RESTAURANTE')")
//    public int updateRestaurant(@RequestBody RestaurantResponseDto restaurantResponseDto) {
    public ResponseEntity<RestaurantResponseDto> updateRestaurant(
            @PathVariable Long rst_id,
            @RequestBody @Valid RestaurantRequestDto restaurantRequestDto
    ) {
            log.info("Solicitud recibida para actualizar el restaurante con ID: {}", rst_id);

        Restaurant restaurant = restaurantService.findRestaurantEntityById(rst_id);

        restaurant.setName(restaurantRequestDto.name());
        restaurant.setDescription(restaurantRequestDto.description());
        restaurant.setCategory(restaurantRequestDto.categoria());
        restaurant.setPhone(restaurantRequestDto.phone());
        restaurant.setAddress(restaurantRequestDto.address());
        restaurant.setLogo(restaurantRequestDto.logo());

        RestaurantResponseDto updatedRestaurant = restaurantService.updateRestaurant(restaurant);
            log.info("Restaurante con ID: {} actualizado exitosamente", rst_id);
            return ResponseEntity.ok(updatedRestaurant);
    }
    @GetMapping("/byOwnerId/{ownerId}")
//    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<List<RestaurantResponseDto>> getByOwnerId(
            @PathVariable Long ownerId
    ) {
        log.info("Solicitud recibida para obtener todos los restaurantes del dueno {}", ownerId);
        List<RestaurantResponseDto> restaurants = restaurantService.findRestaurantsByOwnerId(ownerId);

        log.info("Se recuperaron {} restaurantes exitosamente para el dueño {}.", restaurants.size(), ownerId);
        return ResponseEntity.ok(restaurants);
    }

    /**
     * Endpoint to retrieve the categories offered by a specific restaurant.
     * Delegates the retrieval logic to {@link IRestaurantService#getOfferedCategories(Long)}.
     *
     * @param restaurantId The ID of the restaurant for which to retrieve the offered categories.
     * @return A set of {@link Category} objects representing the categories offered by the restaurant.
     */
    @GetMapping("{restaurantId}/categories")
    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<Set<Category>> getOfferedCategories(@PathVariable Long restaurantId) {
        log.info("Solicitud recibida para obtener las categorias ofrecidas por el restaurante {}", restaurantId);
        Set<Category> categories = restaurantService.getOfferedCategories(restaurantId);

        log.info("Se recuperaron {} categorias exitosamente para el restaurante {}.", categories.size(), restaurantId);
        return ResponseEntity.ok(categories);
    }
    @PostMapping("{restaurantId}/categories")
    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<CategoryResponseDto> addCategoryToRestaurant(
            @PathVariable Long restaurantId,
            @Valid @RequestBody CategoryRequestDto categoryInput
            ) {
        log.info("Solicitud recibida para añadir la categoria {} al restaurante con ID {}", categoryInput.name(), restaurantId);

        Category associatedCategory = restaurantService.addCategoryToRestaurant(restaurantId, categoryInput);
        // Mapea la entidad devuelta a un DTO de respuesta
        CategoryResponseDto associatedCategoryResponseDto = new CategoryResponseDto(
                associatedCategory.getId(),
                associatedCategory.getName(),
                associatedCategory.getDescription()
        );

        log.info("Categoría {} asociada/creada para restaurante {}. Devolviendo DTO.",
                associatedCategoryResponseDto.name(), restaurantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(associatedCategoryResponseDto);
    }



    /**
     * Endpoint to delete an existing restaurant in the system using the provided {@link RestaurantRequestDto}.
     * Delegates the delete logic to {@link IRestaurantService#deleteById(Long)}.
     *
     * @param rst_id The ID of the restaurant to delete.
     * @return A {@link ResponseEntity} object with a status of 204 No Content.
     */
    @DeleteMapping("/{rst_id}")
    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long rst_id) {
            log.info("Solicitud recibida para eliminar el restaurante con ID: {}", rst_id);
            restaurantService.deleteById(rst_id);
            log.info("Restaurante con ID: {} eliminado exitosamente", rst_id);
            return ResponseEntity.noContent().build();
    }
}