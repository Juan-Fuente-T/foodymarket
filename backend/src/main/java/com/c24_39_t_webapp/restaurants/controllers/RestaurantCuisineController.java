package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.dtos.response.CuisineResponseDto;
import com.c24_39_t_webapp.restaurants.services.IRestaurantCuisineService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
    @Slf4j
    @RestController
    @AllArgsConstructor
    @RequestMapping("/api/cuisines")
    public class RestaurantCuisineController {

    private final IRestaurantCuisineService cuisineService;
    @GetMapping
    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<List<CuisineResponseDto>> getAllCuisines() {
        log.info("Obteniendo la lista de tipos de restaurante");
        // Llama al servicio para obtener la lista de DTOs
        List<CuisineResponseDto> cuisines = cuisineService.findAll();
        log.info("Lista de tipos de restaurante obtenida: {}", cuisines);
        // Devuelve la lista en la respuesta con estado OK (200)
        return ResponseEntity.ok(cuisines);
    }
}

