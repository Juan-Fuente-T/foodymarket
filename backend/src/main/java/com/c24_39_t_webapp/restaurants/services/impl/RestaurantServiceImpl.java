package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.request.CategoryRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.exception.UserNotFoundException;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.RestaurantCuisine;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.RestaurantCuisineRepository;
import com.c24_39_t_webapp.restaurants.repository.RestaurantRepository;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
import com.c24_39_t_webapp.restaurants.services.ICategoryService;
import com.c24_39_t_webapp.restaurants.services.IRestaurantService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class RestaurantServiceImpl implements IRestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final ICategoryService categoryService;
    private final RestaurantCuisineRepository cuisineRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public RestaurantResponseDto registerRestaurant(RestaurantRequestDto restaurantRequestDto, String email) {
        log.info("Intentando crear un restaurante para el usuario con email: {}", email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Intento fallido: Usuario con email {} no encontrado", email);
                    return new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no registrado");
                });
        if (!user.getRole().equals("RESTAURANTE")) {
            log.warn("Intento fallido: Usuario con el Rol {} no esta autorizado", user.getRole());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para crear un Restaurante");
        }

        RestaurantCuisine cuisine = cuisineRepository.findById(restaurantRequestDto.cuisineId())
                .orElseThrow(() -> { // <<< Maneja el Optional<> devuelto por findById
                    log.warn("Tipo de cocina con ID {} no encontrado.", restaurantRequestDto.cuisineId());
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de cocina inválido: ID "
                            + restaurantRequestDto.cuisineId());
                });
        Restaurant restaurant = new Restaurant();
        restaurant.setUserEntity(user);
        restaurant.setName(restaurantRequestDto.name());
        restaurant.setDescription(restaurantRequestDto.description());
        restaurant.setCuisine(cuisine);
        restaurant.setPhone(restaurantRequestDto.phone());
        restaurant.setEmail(restaurantRequestDto.email());
        restaurant.setAddress(restaurantRequestDto.address());
        restaurant.setOpeningHours(restaurantRequestDto.openingHours());
        restaurant.setLogo(restaurantRequestDto.logo());
        restaurant.setCoverImage(restaurantRequestDto.coverImage());

//        restaurantRepository.save(restaurant);
//        log.info("¡Restaurante con ID {} creado Exitosamente!", restaurant.getId());
        entityManager.persist(restaurant); // persist() es para entidades NUEVAS
        entityManager.flush(); // Fuerza la ejecución del SQL (INSERT) ahora mismo

        log.info("¡Restaurante persistido! ID asignado por BD debería ser: {}", restaurant.getId());

        return new RestaurantResponseDto(
                restaurant.getId(),
                user.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getPhone(),
                restaurant.getEmail(),
                restaurant.getAddress(),
                restaurant.getOpeningHours(),
                restaurant.getLogo(),
                restaurant.getCoverImage(),
                (cuisine != null) ? cuisine.getId() : null,
                (cuisine != null) ? cuisine.getName() : null
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<RestaurantResponseDto> findAll() {
        List<Restaurant> restaurants = restaurantRepository.findAll();

        if (restaurants.isEmpty()) return Collections.emptyList();
        log.info("Se encontraron {} restaurantes", restaurants.size());

        return restaurants.stream()
                .map(restaurant -> {
                    RestaurantCuisine cuisine = restaurant.getCuisine();

                    return new RestaurantResponseDto(
                            restaurant.getId(),
                            (restaurant.getUserEntity() != null) ? restaurant.getUserEntity().getId() : null,
                            restaurant.getName(),
                            restaurant.getDescription(),
                            restaurant.getPhone(),
                            restaurant.getEmail(),
                            restaurant.getAddress(),
                            restaurant.getOpeningHours(),
                            restaurant.getLogo(),
                            restaurant.getCoverImage(),
                            (cuisine != null) ? cuisine.getId() : null,
                            (cuisine != null) ? cuisine.getName() : null
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public RestaurantResponseDto findById(Long id) {
        log.info("Buscando restaurante con ID: {}", id);
//        Restaurant restaurant = restaurantRepository.findById(id)
        if (id == null || id <= 0) {
            log.warn("El ID del restaurante proporcionado es invalido: {}", id);
            throw new RestaurantNotFoundException("El ID del restaurante no es válido " + id);
        }
        return restaurantRepository.findById(id)
                .map(restaurant -> {
                    RestaurantCuisine cuisine = restaurant.getCuisine();

                    RestaurantResponseDto dto = new RestaurantResponseDto(
                            restaurant.getId(),
                            (restaurant.getUserEntity() != null) ? restaurant.getUserEntity().getId() : null,
                            restaurant.getName(),
                            restaurant.getDescription(),
                            restaurant.getPhone(),
                            restaurant.getEmail(),
                            restaurant.getAddress(),
                            restaurant.getOpeningHours(),
                            restaurant.getLogo(),
                            restaurant.getCoverImage(),
                            (cuisine != null) ? cuisine.getId() : null,
                            (cuisine != null) ? cuisine.getName() : null
                    );
                    log.info("DTO creado en findById ANTES de retornar: {}", dto);
                    return dto;
                })
                .orElseThrow(() -> {
                    log.warn("No se encontro un restaurante con el ID: {}", id);
                    return new RestaurantNotFoundException("No se encontro un restaurante con ese ID: " + id);
                });
    }

    @Transactional
    @Override
    public RestaurantResponseDto updateRestaurant(RestaurantRequestDto restaurantRequestDto, Long rst_id) {
//        if (!restaurantRepository.existsById(rst_id)) {
//            throw new RestaurantNotFoundException("Restaurante no encontrado con id: " + rst_id);
//        }
        log.info("Actualizando el restaurante con ID: {}", rst_id);
        Restaurant newRestaurant = restaurantRepository.findById(rst_id)
                .orElseThrow(() -> {
                    log.warn("No se encontró un restaurante con ese ID para editar: {}", rst_id);
                    return new RestaurantNotFoundException(("No se encontró un restaurante con ese ID para editar: " + rst_id));
                });

        RestaurantCuisine cuisine = cuisineRepository.findById(restaurantRequestDto.cuisineId())
                .orElseThrow(() -> { // <<< Maneja el Optional<> devuelto por findById
                    log.warn("Tipo de cocina con ID {} no encontrado.", restaurantRequestDto.cuisineId());
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de cocina inválido: ID "
                            + restaurantRequestDto.cuisineId());
                });

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (newRestaurant.getUserEntity() == null || !newRestaurant.getUserEntity().getEmail().equals(userEmail)) {
            log.warn("Permiso denegado: Usuario {} intentando actualizar restaurante {}", userEmail, rst_id);
            throw new SecurityException("No tienes permiso para actualizar este restaurante");
        }

        newRestaurant.setName(restaurantRequestDto.name());
        newRestaurant.setDescription(restaurantRequestDto.description());
        newRestaurant.setCuisine(cuisine);
        newRestaurant.setPhone(restaurantRequestDto.phone());
        newRestaurant.setEmail(restaurantRequestDto.email());
        newRestaurant.setAddress(restaurantRequestDto.address());
        newRestaurant.setOpeningHours(restaurantRequestDto.openingHours());
        newRestaurant.setLogo(restaurantRequestDto.logo());
        newRestaurant.setCoverImage(restaurantRequestDto.coverImage());

        Restaurant updatedRestaurant = restaurantRepository.save(newRestaurant);

        log.info("Restaurante actualizado exitosamente: {}", updatedRestaurant);
        return new RestaurantResponseDto(
                updatedRestaurant.getId(),
                updatedRestaurant.getUserEntity().getId(),
                updatedRestaurant.getName(),
                updatedRestaurant.getDescription(),
                updatedRestaurant.getPhone(),
                updatedRestaurant.getEmail(),
                updatedRestaurant.getAddress(),
                updatedRestaurant.getOpeningHours(),
                updatedRestaurant.getLogo(),
                updatedRestaurant.getCoverImage(),
                (cuisine != null) ? cuisine.getId() : null,
                (cuisine != null) ? cuisine.getName() : null
        );
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new RestaurantNotFoundException("Restaurante no encontrado con id: " + id);
        }
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity owner = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Dueño de restaurante no encontrado con ID: " + id));
        if (!owner.getEmail().equals(authenticatedUsername /* && !SecurityUtils.isAdmin() */ )) { // Añadir lógica si admin puede ver
            log.warn("Usuario {} intentando acceder a restaurantes del dueño {}", authenticatedUsername, id);
            throw new UnauthorizedAccessException("No tienes permiso para ver los restaurantes de este dueño.");
        }
        restaurantRepository.deleteById(id);
    }

//    private Restaurant getRestaurantById(Long id) {
//        return restaurantRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("El restaurante no existe!"));
//    }


    @Transactional(readOnly = true)
    @Override
    public List<RestaurantResponseDto> findRestaurantsByOwnerId(Long ownerId) { // Este devuelve un array de  Restaurant
        log.info("Buscando los restaurantes del dueño con id {}", ownerId);
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Dueño no encontrado con ID: " + ownerId));
        if (!owner.getEmail().equals(authenticatedUsername)) {
            log.warn("Usuario {} intentando acceder a restaurantes del dueño {}", authenticatedUsername, ownerId);
            throw new SecurityException("No tienes permiso para ver los restaurantes de este dueño.");
        }
        List<RestaurantResponseDto> dtos = restaurantRepository.findRestaurantsByOwnerId(ownerId);
        if(dtos.isEmpty()) {
            log.info("No se encontraron restaurantes para el dueño con ID {}", ownerId);
            return Collections.emptyList();
        }

        log.info("Se encontraron {} DTOs de restaurantes para el propietario {}", dtos.size(), ownerId);
        return dtos;
    }

    @Transactional(readOnly = true)
    @Override
    public Set<CategoryResponseDto> findByIdFetchingCategories(Long restaurantId) {
        log.info("Obteniendo las categorias de producto del restaurantes on id {}", restaurantId);
        Restaurant restaurant = restaurantRepository.findByIdFetchingCategories(restaurantId).orElseThrow(() ->
                new RestaurantNotFoundException(
                        "Restaurante no encontrado con ID: " + restaurantId
                ));
        log.info("Restaurante '{}' encontrado. Accediendo a sus categorías cargadas.", restaurant.getName());

        // Se accede directamente a la colección. Ya está cargada gracias al JOIN FETCH/EntityGraph.
        Set<Category> categories = restaurant.getOfferedCategories();
        log.info("Se encontraron {} categorias para el restaurante con ID {}", categories.size(), restaurantId);
        if (categories.isEmpty()) {
            log.info("No se encontraron categorías para el restaurante con ID {}", restaurantId);
            return Collections.emptySet();
        }

        return categories.stream()
                .map(category -> new CategoryResponseDto(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ))
                .collect(Collectors.toSet());
    }

    @Transactional
    @Override
    public CategoryResponseDto addCategoryToRestaurant(Long restaurantId, CategoryRequestDto categoryInput) {
        log.info("Intentando asociar la categoría '{}' al restaurante con ID {}", categoryInput.name(), restaurantId);
        Category categoryToAdd = categoryService.findOrCreateCategory(categoryInput);

        Restaurant restaurant = restaurantRepository.findByIdFetchingCategories(restaurantId)
                .orElseThrow(() -> {
                    log.error("Restaurante con ID {} no encontrado al intentar añadir categoría.", restaurantId);
                    return new EntityNotFoundException("Restaurante no encontrado con ID: " + restaurantId);
                });
        boolean added = restaurant.getOfferedCategories().add(categoryToAdd);

        // Esto hará que JPA/Hibernate inserte la fila en la tabla categorias_restaurante
        if (added) {
            log.info("Asociando la categoría '{}' (ID: {}) al restaurante '{}' (ID: {})",
                    categoryToAdd.getName(), categoryToAdd.getId(), restaurant.getName(), restaurant.getId());
            restaurantRepository.save(restaurant);
            log.info("Asociación guardada en tabla intermedia.");
        } else {
            log.info("La categoría '{}' (ID: {}) ya estaba asociada al restaurante '{}' (ID: {}). No se requiere guardar.",
                    categoryToAdd.getName(), categoryToAdd.getId(), restaurant.getName(), restaurant.getId());
        }
        return new CategoryResponseDto(
                categoryToAdd.getId(),
                categoryToAdd.getName(),
                categoryToAdd.getDescription()
        );
    }
}

