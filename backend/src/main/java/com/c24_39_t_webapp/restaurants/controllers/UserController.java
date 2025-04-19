package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.services.IUserService;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private IUserService userService;

//    @PostMapping
//    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody final UserRequestDto userCreateRequestDto) {
//    UserEntity user = new UserEntity();
//        user.setEmail(userCreateRequestDto.email());
//        user.setPassword(userCreateRequestDto.password());
//        user.setName(userCreateRequestDto.name());
//        user.setPhone(userCreateRequestDto.phone());
//        user.setAddress(userCreateRequestDto.address());
//        UserResponseDto newUser = userService.createUser(user);
////        return ResponseEntity.ok(newUser);
//        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
//    }

    @PutMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody final UserRequestDto userRequestDto,
                                                      @AuthenticationPrincipal final UserDetailsImpl userDetails) {
        String username = userDetails.getUsername();
        UserEntity user = userService.getUserProfile(username); // Obtiene el usuario actual
        user.setName(userRequestDto.name() != null ? userRequestDto.name() : user.getName());
        user.setEmail(userRequestDto.email() != null ? userRequestDto.email() : user.getEmail());
        user.setAddress(userRequestDto.address() != null ? userRequestDto.address() : user.getAddress());
        user.setPhone(userRequestDto.phone() != null ? userRequestDto.phone() : user.getPhone());
        user.setPassword(userRequestDto.password() != null ? userRequestDto.password() : user.getPassword());

        UserResponseDto updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping
    public ResponseEntity<UserResponseDto> getUserProfile(@AuthenticationPrincipal final UserDetailsImpl userDetails) {
        UserResponseDto user = new UserResponseDto(userService.getUserProfile(userDetails.getUsername()));
        return ResponseEntity.ok(user);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal final UserDetailsImpl userDetails) {
        userService.deleteUser(userDetails.getId());
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
