package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.LoginRequest;
import com.c24_39_t_webapp.restaurants.dtos.request.RegisterRequest;
import com.c24_39_t_webapp.restaurants.dtos.response.AuthResponse;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.config.segurity.JwtUtil;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthResponse register(RegisterRequest request){
        if (userRepository.existsByEmail(request.email())){
            throw new RuntimeException("Usuario ya existe");
        }

        var newUser = UserEntity.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .address(request.address())
                .role(request.role().toUpperCase())
                .build();

        var savedUser = userRepository.save(newUser);
        UserResponseDto userResponse = new UserResponseDto(savedUser);

        return new AuthResponse("", "Usuario creado exitosamente", userResponse);
    }

    public AuthResponse login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtUtil.generateToken(request.email(), userDetails.getRole(), userDetails.getId());
        UserResponseDto userResponse = new UserResponseDto(userDetails);
        return new AuthResponse(token, "El usuario ha iniciado sesión correctamente",userResponse);
    }

}
