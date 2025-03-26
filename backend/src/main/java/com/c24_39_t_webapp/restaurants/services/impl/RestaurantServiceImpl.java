package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.user_implementations.ResourceNotFoundException;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.RestaurantRepository;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
import com.c24_39_t_webapp.restaurants.services.IRestaurantService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class RestaurantServiceImpl implements IRestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public RestaurantResponseDto registerRestaurant(Restaurant restaurant, String email) {
        log.info("Intentando crear un restaurante para el usuario con email: {}", email);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Intento fallido: Usuario con email {} no encontrado", email);
                    return new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no registrado");
                });
        if (!user.getRole().equals("RESTAURANT")) {
            log.warn("Intento fallido: Usuario con el Rol {} no esta autorizado", user.getRole());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para crear un Restaurante");
        }

        restaurant.setUserEntity(user);

        restaurantRepository.save(restaurant);
        log.info("¡Restaurante creado Exitosamente!");
        return new RestaurantResponseDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getCategoria(),
                restaurant.getPhone(),
                restaurant.getAddress(),
                restaurant.getLogo()
        );
    }


    @Override
    public List<RestaurantResponseDto> findAll() {
        List<Restaurant> restaurants = restaurantRepository.findAll();

        if (restaurants.isEmpty()) {
            throw new RuntimeException("No se encontraron restaurantes.");
        }

        return restaurants.stream()
                .map(restaurant -> new RestaurantResponseDto(
                        restaurant.getId(),
                        restaurant.getName(),
                        restaurant.getDescription(),
                        restaurant.getCategoria(),
                        restaurant.getPhone(),
                        restaurant.getAddress(),
                        restaurant.getLogo()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public RestaurantResponseDto findById(Long id) {
        log.info("Buscando restaurante con ID: {}", id);
//        Restaurant restaurant = restaurantRepository.findById(id)
        if (id == null || id <= 0) {
            log.warn("El ID del restaurante proporcionado es invalido: {}", id);
            throw new RestaurantNotFoundException("El ID del restaurante no es válido " + id);
        }
        return restaurantRepository.findById(id)
                .map(restaurant -> new RestaurantResponseDto(
                        restaurant.getId(),
                        restaurant.getName(),
                        restaurant.getDescription(),
                        restaurant.getCategoria(),
                        restaurant.getPhone(),
                        restaurant.getAddress(),
                        restaurant.getLogo()
                ))
                .orElseThrow(() -> {
                    log.warn("No se encontro un gasto con el ID: {}", id);
                    return new RestaurantNotFoundException("No se encontro un restaurante con ese ID: " + id);
                });
    }

    @Override
    public RestaurantResponseDto updateRestaurant(Restaurant restaurant) {
        if (!restaurantRepository.existsById(restaurant.getId())) {
            throw new RestaurantNotFoundException("Restaurante no encontrado con id: " + restaurant.getId());
        }
        log.info("Actualizando el restaurante con ID: {}", restaurant.getId());

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);

        log.info("Restaurante actualizado exitosamente: {}", updatedRestaurant);
        return new RestaurantResponseDto(
                updatedRestaurant.getId(),
                updatedRestaurant.getName(),
                updatedRestaurant.getDescription(),
                restaurant.getCategoria(),
                updatedRestaurant.getPhone(),
                updatedRestaurant.getAddress(),
                updatedRestaurant.getLogo()
        );
    }
    @Override
    public void deleteById(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new RestaurantNotFoundException("Restaurante no encontrado con id: " + id);
        }
        restaurantRepository.deleteById(id);
    }
    private Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El restaurante no existe!"));
    }
    @Override
    public Restaurant findRestaurantEntityById(Long id) { // Este devuelve un Restaurant
        log.info("Buscando el restaurante con ID: {} para actualización", id);
        return restaurantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("No se encontró un restaurante con ese ID para editar: {}", id);
                    return new RestaurantNotFoundException(("No se encontró un restaurante con ese ID para editar: " + id));
                });
    }
}
