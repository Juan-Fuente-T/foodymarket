package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.response.CuisineResponseDto;
import com.c24_39_t_webapp.restaurants.models.RestaurantCuisine;
import com.c24_39_t_webapp.restaurants.repository.RestaurantCuisineRepository;
import com.c24_39_t_webapp.restaurants.services.IRestaurantCuisineService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RestaurantCuisineServiceImpl implements IRestaurantCuisineService {

//    @Autowired
    private final RestaurantCuisineRepository cuisineRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CuisineResponseDto> findAll() {
        List<RestaurantCuisine> cuisines = cuisineRepository.findAll();
        if(cuisines.isEmpty()) return Collections.emptyList();

        return cuisines.stream()
                .map(cuisineEntity -> new CuisineResponseDto(
                        cuisineEntity.getId(),
                        cuisineEntity.getName()
                ))
                .collect(Collectors.toList());
    }
}