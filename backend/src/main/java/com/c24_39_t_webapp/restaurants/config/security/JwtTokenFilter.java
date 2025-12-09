package com.c24_39_t_webapp.restaurants.config.security;

import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);
                String id = jwtUtil.extractId(token);

                if (email != null && role != null) {
                    // Crea las autoridades a partir del ROL en el token
//                    List<GrantedAuthority> authorities = Collections.singletonList(
//                            new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
//                    );
//
//                    // Se puede usar el email como principal, o crear un UserDetails simple si se necesita
//                    // Aquí se usa un User de Spring Security que es una implementación UserDetails simple
//                    UserDetails userDetailsPrincipal = new org.springframework.security.core.userdetails.User(
//                            email, "", authorities
//                    );
//                    // O si se quiere el UserDetailsImpl con más datos pero sin BD:
//                    // UserDetailsImpl userDetailsPrincipal = UserDetailsImpl.buildFromToken(userId, email, role, authorities);
//                    // (Se necesitaría un constructor estático `buildFromToken` en UserDetailsImpl que NO llame a la BD)
//
//                    UsernamePasswordAuthenticationToken authentication =
//                            new UsernamePasswordAuthenticationToken(
//                                    userDetailsPrincipal,
//                                    null,
//                                    userDetailsPrincipal.getAuthorities());

                    // Crea UserEntity VACÍO (sin llamadas a base de datos)
                    UserEntity userEntity = new UserEntity();
                    userEntity.setId(Long.parseLong(id));
                    userEntity.setEmail(email);
                    userEntity.setRole(role);
                    userEntity.setPassword("");

                    // Usa el UserDetailsImpl ACTUAL
                    UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

                    //Crea y SETEA autenticación
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Actualiza Details del usuario y contexto de seguridad
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}

