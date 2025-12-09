//package com.c24_39_t_webapp.restaurants.config.security.integration;
package com.c24_39_t_webapp.restaurants.integration;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.config.security.JwtUtil;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de INTEGRACIÓN para JwtTokenFilter
 *
 * ✅ PROPÓSITO: Probar que el filter intercepta, valida y propaga autenticación correctamente
 *
 * ✅ Qué prueba:
 * - Token válido → Usuario autenticado en SecurityContext
 * - Token inválido → Usuario NO autenticado (401)
 * - Sin token → Usuario NO autenticado (401)
 *
 * ❌ Qué NO prueba (responsabilidad de otros tests):
 * - Lógica específica de cada endpoint (UserControllerUpdateTests, ProductControllerDeleteTests, etc.)
 * - Manejo de permisos (ProductServiceDeleteUnitTests, etc.)
 * - Casos edge (responsabilidad del controller o service)
 *
 * ✅ Filter crea UserDetailsImpl DIRECTAMENTE del token (SIN llamar a BD)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Slf4j
@DisplayName("JwtTokenFilter - Tests de Integración")
class JwtTokenFilterIntegrationTests {

    private static final String USER_EMAIL = "user@test.com";
    private static final String USER_ROLE = "CLIENTE";
    private static final Long USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() {
        UserEntity testUser = EntityModelFactory.userEntity(null, USER_EMAIL);
//        UserEntity testUser = UserEntity.builder()
//                .email(USER_EMAIL)
//                .name("Test User")
//                .role(USER_ROLE)
//                .phone("555555555")
//                .address("Test Address")
//                .password("encoded_password")
//                .build();
        userRepository.save(testUser);
        userRepository.flush(); // ← Fuerza escritura a BD

        // ✅ Generar token válido
        validToken = jwtUtil.generateToken(USER_EMAIL, USER_ROLE, USER_ID);

        // ✅ Token inválido (corrupto)
        invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.payload";

        log.info("✅ Setup: Token válido generado para {}", USER_EMAIL);
    }
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * ✅ TEST 1: Token válido en GET
     *
     * Verificación:
     * - Token se valida correctamente
     * - Usuario se carga del token en SecurityContext
     * - Request es procesada (200 OK)
     */
    @Test
    @DisplayName("Token válido en GET → 200 OK (usuario autenticado)")
    void whenTokenIsValid_thenUserIsAuthenticated() throws Exception {
        mockMvc.perform(get("/api/user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andDo(result -> log.info("✅ Token válido: Usuario autenticado en SecurityContext"));
    }

    /**
     * ✅ TEST 2: Token inválido
     *
     * Verificación:
     * - Token corrupto es rechazado
     * - Usuario NO se carga en SecurityContext
     * - Retorna 401 Unauthorized
     */
    @Test
    @DisplayName("Token inválido → 401 Unauthorized (usuario NO autenticado)")
    void whenTokenIsInvalid_thenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/user")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andDo(result -> log.info("✅ Token inválido: Usuario NO autenticado"));
    }

    /**
     * ✅ TEST 3: Sin token
     *
     * Verificación:
     * - Sin Authorization header
     * - Usuario NO se carga
     * - Retorna 401 Unauthorized
     */
    @Test
    @DisplayName("Sin token → 401 Unauthorized (usuario NO autenticado)")
    void whenNoToken_thenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isUnauthorized())
                .andDo(result -> log.info("✅ Sin token: Usuario NO autenticado"));
    }
}