package com.c24_39_t_webapp.restaurants.config.segurity;

// --- Importaciones para CORS ---

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import com.c24_39_t_webapp.restaurants.services.CustomUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final CustomUserDetailService userDetailService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers(
                                "/api/public/**", "/auth/**", "/h2-console/**", "/api/restaurant/testMethod",
                                "/api/restaurant/testPostMethod",
//                        Swagger paths
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**").permitAll()
                        // Consultas públicas (lectura para todos)
                        .requestMatchers(HttpMethod.GET, "/api/category/**", "/api/restaurant/**",
                                "/api/product/**").permitAll()
                        .requestMatchers("/api/category/**", "/api/restaurant/**",
                                "/api/product/**").hasRole("RESTAURANTE")  // Orders: Cliente solo crea (POST)
                        .requestMatchers(HttpMethod.POST, "/api/order/**").hasRole("CLIENTE")
                        // Orders: Cliente solo consulta (GET) por fecha y cliente
                        .requestMatchers(HttpMethod.GET, "/api/order/byClientDate",
                                "/api/order/byClientId/{cln_id}").hasRole("CLIENTE")
                        // Orders: RESTAURANTE gestiona lo demás (GET, PATCH, DELETE)
                        .requestMatchers(HttpMethod.GET, "/api/order/**").hasRole("RESTAURANTE")
                        .requestMatchers(HttpMethod.PATCH, "/api/order/**").hasRole("RESTAURANTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/order/**").hasRole("RESTAURANTE")
                        // Rutas exclusivas de RESTAURANTE, salvo GET
                        .requestMatchers("/api/category/**", "/api/restaurant/**", "/api/cuisines/**",
                                "/api/product/**").hasRole("RESTAURANTE")
                        // Rutas exclusivas de cliente
                        .requestMatchers("/api/user/**").hasRole("CLIENTE")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "https://foodymarket.vercel.app/", // URL del front en Vercel
                "http://localhost:8081", "http://localhost:8080"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control", "X-Requested-With")); // Cabeceras comunes
        configuration.setAllowCredentials(true); // Importante para que el navegador envíe el token en la cabecera Auth
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration); // Aplica la configuración a todas las rutas bajo /api/**
        return source;
    }
}

