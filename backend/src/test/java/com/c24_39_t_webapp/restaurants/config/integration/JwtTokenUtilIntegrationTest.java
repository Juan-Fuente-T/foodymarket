package com.c24_39_t_webapp.restaurants.config.integration;

import com.c24_39_t_webapp.restaurants.config.security.JwtUtil;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("Flujo Completo - Generar, Extraer, Validar Token JWT")
public class JwtTokenUtilIntegrationTest {
    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "user@test.com";
    private static final String USER_ROLE = "CLIENTE";
    private static final String SECRET_KEY = "KsFJSSRNP18u9VMBrimE2UvcBvMQG0SJAwztTdpgYOs=";
    private static final long EXPIRATION_TIME = 3600000; // 1 hora en ms

    @Spy
    private JwtUtil jwtUtil = new JwtUtil();

    // Configura el JWTUtil con valores de prueba antes del test
    {
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION_TIME);
    }

        /**
         * Test: Flujo completo de generación y validación
         *
         * Verificación:
         * ✅ Generar token
         * ✅ Extraer claims
         * ✅ Validar token
         * ✅ Verificar que no está expirado
         */
        @Test
        @DisplayName("Flujo completo → Todos los pasos exitosos")
        @Transactional
        void whenCompletingFullFlow_thenAllStepsSucceed() {
            // 1. GENERATE
            String token = jwtUtil.generateToken(USER_EMAIL, USER_ROLE, USER_ID);
            assertNotNull(token, "Paso 1: Token generado");

            // 2. EXTRACT
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            String id = jwtUtil.extractId(token);

            assertEquals(USER_EMAIL, email, "Paso 2a: Email extraído");
            assertEquals(USER_ROLE, role, "Paso 2b: Role extraído");
            assertEquals(USER_ID.toString(), id, "Paso 2c: ID extraído");

            // 3. VALIDATE
            boolean isValid = jwtUtil.validateToken(token);
            assertTrue(isValid, "Paso 3: Token válido");

            // 4. CHECK EXPIRATION
            boolean isExpired = jwtUtil.isExpiredToken(token);
            assertFalse(isExpired, "Paso 4: Token no expirado");

            // 5. COMPLETE VALIDATION
            UserEntity user = EntityModelFactory.clientEntity(Long.parseLong(id), email);
            boolean isCompletelyValid = jwtUtil.isValidToken(token, user);
            assertTrue(isCompletelyValid, "Paso 5: Validación completa exitosa");

            log.info("✅ Flujo completo: Todos los pasos exitosos");
        }
}