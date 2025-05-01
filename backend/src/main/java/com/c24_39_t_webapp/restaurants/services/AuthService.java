package com.c24_39_t_webapp.restaurants.services;

import com.c24_39_t_webapp.restaurants.dtos.request.LoginRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.AuthResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
import com.c24_39_t_webapp.restaurants.config.segurity.JwtUtil;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final IUserService userService;
    private final UserRepository userRepository;
    public AuthResponseDto register (UserRequestDto registerDto){
        if (userRepository.existsByEmail(registerDto.email())){
            throw new RuntimeException("Usuario ya existe con email: " + registerDto.email());
        }

        UserResponseDto userResponseDto = userService.createUser(registerDto);

        // Generar el token JWT
        String token = jwtUtil.generateToken(
                userResponseDto.email(),
                userResponseDto.role(),
                userResponseDto.id()
        );
        return new AuthResponseDto(token, "Usuario creado exitosamente", userResponseDto);
    }

    public AuthResponseDto login(LoginRequestDto request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        UserResponseDto userResponse = userService.getUserProfile(request.email());

        String token = jwtUtil.generateToken(
                request.email(),
                userResponse.role(),
                userResponse.id()
        );

        return new AuthResponseDto(token, "El usuario ha iniciado sesión correctamente",userResponse);
    }
}
