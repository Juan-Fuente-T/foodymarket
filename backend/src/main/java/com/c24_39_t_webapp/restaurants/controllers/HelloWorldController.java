package com.c24_39_t_webapp.restaurants.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    // RUTA HELLO PROTEGIDA PERO ACCESIBLE PARA CUALQUIER ROL
    @GetMapping("/hello")
    public ResponseEntity<?> hello(){
        return ResponseEntity.ok("Hola Mundo");
    }

    // RUTA HELLO PUBLICA (CON O SIN AUTENTICACION)
    @GetMapping("/api/public/hello")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> helloPublic(){
        return ResponseEntity.ok("Funciona");
    }

    // RUTA PROTEGIDA, SOLO PARA ROLE_RESTAURANTE
//    @GetMapping("/api/restaurant")
//    @PreAuthorize("hasRole(ROLE_RESTAURANT)")
//    public ResponseEntity<?> helloRestaurant(){
//        return ResponseEntity.ok("Hello Role Resstaurante");
//    }

    // RUTA PROTEGIDA SOLO PARA ROLE_USUARIO
    //@GetMapping("/api/user")
    //@PreAuthorize("hasRole(ROLE_USER)")
    //public ResponseEntity<?> helloUser(){
    //    return ResponseEntity.ok("Hello Role User");
    //}

}
