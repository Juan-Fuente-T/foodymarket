package com.c24_39_t_webapp.restaurants.integration;

import com.c24_39_t_webapp.restaurants.config.security.JwtUtil;
import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.factories.UserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.c24_39_t_webapp.restaurants.integration.support.LoginRequest;

/**
 * INTEGRATION TEST: Registration → Login → Use Token
 *
 * ✅ Flujo crítico: Usuario se registra → obtiene token → usa token en request
 * ✅ POST /api/auth/register (crea usuario)
 * ✅ POST /api/auth/login (obtiene token)
 * ✅ GET /api/user con token (accede a recurso protegido)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@DisplayName("Integration Test - Registration & Login Flow")
class RegistrationAndLoginFlowIntegrationTest {

    private static final String REGISTER_ENDPOINT = "/api/auth/register";
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String USER_PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * ✅ FLUJO COMPLETO: Registrar → Login → Usar Token
     *
     * 1. POST /api/auth/register → Usuario creado (201)
     * 2. POST /api/auth/login → Token obtenido (200)
     * 3. GET /api/user con token → Recurso accesible (200)
     */
    @Test
    @DisplayName("FLUJO: Register → Login → Token válido en request protegida")
    void whenRegisteringAndLoggingIn_thenCanAccessProtectedResource() throws Exception {
        long timestamp = System.currentTimeMillis();
        String userEmail = "user-" + timestamp + "@example.com";

        log.info("=== INICIO: Registration & Login Flow ===");

        // 1️⃣ REGISTER
//        UserRequestDto userRegisterDto = UserFactory.defaultRequest();
        UserRequestDto userRegisterDto = UserFactory.requestWith(
                "Atlántico",
                userEmail,
                "RESTAURANTE",//Debe ser role RESTAURANTE
                "555 666 777",
                "Calle Arriba 11",
                "password123");
        String registerPayload = objectMapper.writeValueAsString(userRegisterDto);

        mockMvc.perform(post(REGISTER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
//                .andExpect(status().isCreated())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(userEmail))
                .andDo(result -> log.info("✅ Step 1: Usuario registrado en BD"))
                .andDo(print());

        // 2️⃣ LOGIN
        String loginPayload = objectMapper.writeValueAsString(
                new LoginRequest(userEmail, USER_PASSWORD)
        );

        String[] tokenHolder = new String[1];

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andDo(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    tokenHolder[0] = objectMapper.readTree(responseBody).get("access_token").asText();
                    log.info("✅ Step 2: Token obtenido");
                });

        // 3️⃣ USE TOKEN EN RECURSO PROTEGIDO
        mockMvc.perform(get("/api/user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenHolder[0]))
                .andExpect(status().isOk())
                .andDo(result -> log.info("✅ Step 3: Recurso protegido accesible con token"));

        log.info("=== FIN: Registration & Login Flow completado exitosamente ===\n");
    }
}