package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
import com.c24_39_t_webapp.restaurants.services.IUserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {

    private IUserService userService;

//    @PostMapping  //El Registro de usuarios se maneja desde AuthController
//    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody final UserRequestDto userCreateRequestDto) {
//        log.info("Creando un nuevo usuario con email: {}", userCreateRequestDto.email());
//        UserResponseDto newUser = userService.createUser(userCreateRequestDto);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
//    }

    @PutMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<UserResponseDto> updateUser(
            @RequestBody final Long userIdToUpdate,
            @RequestBody final UserRequestDto userRequestDto) {
        UserResponseDto updatedUser = userService.updateUser(userIdToUpdate, userRequestDto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping
//    @GetMapping("/profile"")
    public ResponseEntity<UserResponseDto> getUserProfile(
            @AuthenticationPrincipal(expression = "username") String userEmail) {
        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Obteniendo perfil para usuario: {}", userEmail);
        UserResponseDto userDto = userService.getUserProfile(userEmail);
        return ResponseEntity.ok(userDto);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('CLIENTE')")
//    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal final UserDetailsImpl userDetails) {
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal final Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Eliminando usuario con ID: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyAuthority('cliente')")
//    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
//        userService.deleteUser(id);
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }
//    @GetMapping
//    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
//        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
//    }
}
