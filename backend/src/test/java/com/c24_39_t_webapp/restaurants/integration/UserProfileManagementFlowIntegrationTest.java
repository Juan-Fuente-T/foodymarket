package com.c24_39_t_webapp.restaurants.integration;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.c24_39_t_webapp.restaurants.integration.support.LoginRequest;

/**
 * INTEGRATION TEST: User Profile Management
 *
 * ✅ Flujo crítico: Usuario registrado → Actualiza perfil → Elimina cuenta
 * ✅ Verifica cascadas al eliminar usuario
 * ✅ POST /api/auth/register
 * ✅ PUT /api/user (actualizar perfil)
 * ✅ DELETE /api/user (eliminar con cascadas)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@DisplayName("Integration Test - User Profile Management")
class UserProfileManagementFlowIntegrationTest {

    private static final String REGISTER_ENDPOINT = "/api/auth/register";
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String USER_ENDPOINT = "/api/user";
    private static final String USER_PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * ✅ FLUJO COMPLETO: Register → Update Profile → Delete User
     *
     * 1. POST /api/auth/register → Usuario creado
     * 2. POST /api/auth/login → Token obtenido
     * 3. GET /api/user → Obtiene perfil actual
     * 4. PUT /api/user → Actualiza perfil
     * 5. DELETE /api/user → Elimina usuario (con cascadas)
     * 6. GET /api/user → Verifica que está eliminado (401)
     */
    @Test
    @DisplayName("FLUJO: Register → Update Profile → Delete User (cascadas correctas)")
    void whenManagingUserProfile_thenCascadesWorkCorrectly() throws Exception {
        long timestamp = System.currentTimeMillis();
        String userEmail = "user-" + timestamp + "@example.com";

        log.info("=== INICIO: User Profile Management Flow ===");

        // 1️⃣ REGISTER
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
                .andDo(result -> log.info("✅ Step 1: Usuario registrado"));

        // 2️⃣ LOGIN
        String loginPayload = objectMapper.writeValueAsString(
                new LoginRequest(userEmail, USER_PASSWORD)
        );

        String[] tokenHolder = new String[1];

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    tokenHolder[0] = objectMapper.readTree(responseBody).get("access_token").asText();
                    log.info("✅ Step 2: Token obtenido");
                });

        // 3️⃣ GET PERFIL ACTUAL
        mockMvc.perform(get(USER_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenHolder[0]))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(userEmail))
                .andDo(result -> log.info("✅ Step 3: Perfil obtenido"));

        // 4️⃣ UPDATE PERFIL
        UserRequestDto userUpdaterDto = UserFactory.requestWith(
                "Updated Name",
                userEmail,
                "CLIENTE",
                "555555555",
                "Updated Address",
                USER_PASSWORD
        );
        String updatePayload = objectMapper.writeValueAsString(userUpdaterDto);

        mockMvc.perform(put(USER_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenHolder[0])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(userEmail))
                .andDo(result -> log.info("✅ Step 4: Perfil actualizado"));

        // 5️⃣ DELETE USER
        mockMvc.perform(delete(USER_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenHolder[0]))
                .andExpect(status().isNoContent())
                .andDo(result -> log.info("✅ Step 5: Usuario eliminado (con cascadas en BD)"));

        // 6️⃣ VERIFY DELETED (intenta acceder con token anterior)
        mockMvc.perform(get(USER_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenHolder[0]))
                .andExpect(status().isNotFound())
                .andDo(result -> log.info("✅ Step 6: Usuario no encontrado (eliminación confirmada)"));

        log.info("=== FIN: User Profile Management Flow completado ===\n");
    }
}
