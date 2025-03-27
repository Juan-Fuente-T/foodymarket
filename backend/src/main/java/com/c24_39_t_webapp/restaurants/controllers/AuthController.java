package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.dtos.request.LoginRequest;
import com.c24_39_t_webapp.restaurants.dtos.request.RegisterRequest;
import com.c24_39_t_webapp.restaurants.dtos.response.AuthResponse;
import com.c24_39_t_webapp.restaurants.services.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody final RegisterRequest request){
        final AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody final LoginRequest request){
        final AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

}
