package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitarios para UserDetailsImpl
 *
 * ✅ UserEntity viene de EntityModelFactory
 * ✅ Cobertura de métodos de UserDetails
 * ✅ Happy path + edge cases
 *
 * Cobertura:
 * ✅ getAuthorities() con rol válido
 * ✅ getAuthorities() con rol null
 * ✅ getAuthorities() con rol vacío
 * ✅ getUsername() retorna email
 * ✅ getPassword() retorna password
 * ✅ isAccountNonLocked()
 * ✅ isAccountNonExpired()
 * ✅ isCredentialsNonExpired()
 * ✅ isEnabled()
 */
@Slf4j
@DisplayName("UserDetailsImpl - Unit Tests")
class UserDetailsImplUnitTests {

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "user@example.com";
    private static final String USER_PASSWORD = "encodedPassword123";

    // ==================== GETAUTHORITIES TESTS ====================

    @Nested
    @DisplayName("getAuthorities() - Success Cases")
    class GetAuthoritiesSuccessCases {

        private UserEntity userEntity;
        private UserDetailsImpl userDetails;

        /**
         * Test: getAuthorities() con rol cliente
         *
         * Verificación:
         * ✅ Retorna SimpleGrantedAuthority con "ROLE_CLIENTE"
         * ✅ Size es 1
         */
        @Test
        @DisplayName("Rol cliente → Retorna ROLE_CLIENTE")
        void whenRoleIsCliente_thenReturnsClienteAuthority() {
            // Arrange
            userEntity = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            userEntity.setRole("cliente");
            userEntity.setPassword(USER_PASSWORD);
            userDetails = new UserDetailsImpl(userEntity);

            // Act
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            // Assert
            assertNotNull(authorities, "Authorities no debe ser null");
            assertEquals(1, authorities.size(), "Debe tener exactamente 1 autoridad");

            GrantedAuthority authority = authorities.iterator().next();
            assertEquals("ROLE_CLIENTE", authority.getAuthority(), "Debe ser ROLE_CLIENTE");
        }

        /**
         * Test: getAuthorities() con rol restaurante
         *
         * Verificación:
         * ✅ Retorna SimpleGrantedAuthority con "ROLE_RESTAURANTE"
         */
        @Test
        @DisplayName("Rol restaurante → Retorna ROLE_RESTAURANTE")
        void whenRoleIsRestaurante_thenReturnsRestauranteAuthority() {
            // Arrange
            userEntity = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            userEntity.setRole("restaurante");
            userEntity.setPassword(USER_PASSWORD);
            userDetails = new UserDetailsImpl(userEntity);

            // Act
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            // Assert
            assertNotNull(authorities);
            assertEquals(1, authorities.size());

            GrantedAuthority authority = authorities.iterator().next();
            assertEquals("ROLE_RESTAURANTE", authority.getAuthority(), "Debe ser ROLE_RESTAURANTE");
        }

        /**
         * Test: Rol se convierte a mayúsculas
         *
         * Verificación:
         * ✅ El rol se transforma correctamente a mayúsculas
         */
        @Test
        @DisplayName("Rol en minúsculas → Se convierte a mayúsculas")
        void whenRoleIsLowercase_thenConvertedToUppercase() {
            // Arrange
            userEntity = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            userEntity.setRole("cliente");  // minúsculas
            userEntity.setPassword(USER_PASSWORD);
            userDetails = new UserDetailsImpl(userEntity);

            // Act
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
            GrantedAuthority authority = authorities.iterator().next();

            // Assert
            assertEquals("ROLE_CLIENTE", authority.getAuthority(), "Debe estar en mayúsculas");
            assertNotEquals("ROLE_cliente", authority.getAuthority(), "No debe mantener minúsculas");
        }
    }

    // ==================== GETAUTHORITIES - EDGE CASES ====================

    @Nested
    @DisplayName("getAuthorities() - Edge Cases")
    class GetAuthoritiesEdgeCases {

        private UserEntity userEntity;
        private UserDetailsImpl userDetails;

        /**
         * Test: Rol null
         *
         * Verificación:
         * ✅ Retorna Collections.emptyList()
         * ✅ No lanza excepción
         */
        @Test
        @DisplayName("Rol null → Retorna lista vacía")
        void whenRoleIsNull_thenReturnsEmptyList() {
            // Arrange
            userEntity = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            userEntity.setRole(null);
            userEntity.setPassword(USER_PASSWORD);
            userDetails = new UserDetailsImpl(userEntity);

            // Act
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            // Assert
            assertNotNull(authorities, "Authorities no debe ser null");
            assertTrue(authorities.isEmpty(), "Debe estar vacía cuando rol es null");
            assertEquals(0, authorities.size());
        }

        /**
         * Test: Rol vacío (string)
         *
         * Verificación:
         * ✅ Retorna Collections.emptyList()
         * ✅ No lanza excepción
         */
        @Test
        @DisplayName("Rol vacío → Retorna lista vacía")
        void whenRoleIsEmpty_thenReturnsEmptyList() {
            // Arrange
            userEntity = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            userEntity.setRole("");  // vacío
            userEntity.setPassword(USER_PASSWORD);
            userDetails = new UserDetailsImpl(userEntity);

            // Act
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            // Assert
            assertNotNull(authorities);
            assertTrue(authorities.isEmpty(), "Debe estar vacía cuando rol es vacío");
        }

        /**
         * Test: Rol con espacios en blanco
         *
         * Verificación:
         * ✅ Retorna Collections.emptyList() (porque se trimea)
         */
        @Test
        @DisplayName("Rol con solo espacios → Retorna lista vacía")
        void whenRoleIsOnlyWhitespace_thenReturnsEmptyList() {
            // Arrange
            userEntity = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            userEntity.setRole("   ");  // solo espacios
            userEntity.setPassword(USER_PASSWORD);
            userDetails = new UserDetailsImpl(userEntity);

            // Act
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            // Assert
            assertNotNull(authorities);
            assertTrue(authorities.isEmpty(), "Debe estar vacía cuando rol es solo espacios");
        }
    }

    // ==================== GETUSERNAME TESTS ====================

    @Nested
    @DisplayName("getUsername() Tests")
    class GetUsernameTests {

        private UserEntity userEntity;
        private UserDetailsImpl userDetails;

        @BeforeEach
        void setUp() {
            userEntity = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            userEntity.setPassword(USER_PASSWORD);
            userEntity.setRole("cliente");
            userDetails = new UserDetailsImpl(userEntity);
        }

        /**
         * Test: getUsername() retorna email
         *
         * Verificación:
         * ✅ Retorna el email del UserEntity
         */
        @Test
        @DisplayName("getUsername() → Retorna email")
        void whenGettingUsername_thenReturnsEmail() {
            // Act
            String username = userDetails.getUsername();

            // Assert
            assertNotNull(username);
            assertEquals(USER_EMAIL, username, "Username debe ser el email");
        }
    }

    // ==================== GETPASSWORD TESTS ====================

    @Nested
    @DisplayName("getPassword() Tests")
    class GetPasswordTests {

        private UserEntity userEntity;
        private UserDetailsImpl userDetails;

        @BeforeEach
        void setUp() {
            userEntity = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            userEntity.setPassword(USER_PASSWORD);
            userEntity.setRole("cliente");
            userDetails = new UserDetailsImpl(userEntity);
        }

        /**
         * Test: getPassword() retorna password encoded
         *
         * Verificación:
         * ✅ Retorna el password del UserEntity
         */
        @Test
        @DisplayName("getPassword() → Retorna password")
        void whenGettingPassword_thenReturnsPassword() {
            // Act
            String password = userDetails.getPassword();

            // Assert
            assertNotNull(password);
            assertEquals(USER_PASSWORD, password, "Password debe coincidir");
        }
    }

    // ==================== ACCOUNT STATUS TESTS ====================

    @Nested
    @DisplayName("Account Status Methods")
    class AccountStatusTests {

        private UserEntity userEntity;
        private UserDetailsImpl userDetails;

        @BeforeEach
        void setUp() {
            userEntity = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            userEntity.setPassword(USER_PASSWORD);
            userEntity.setRole("cliente");
            userDetails = new UserDetailsImpl(userEntity);
        }

        /**
         * Test: isAccountNonLocked()
         *
         * Verificación:
         * ✅ Retorna true (implementación por defecto)
         */
        @Test
        @DisplayName("isAccountNonLocked() → Retorna true")
        void whenCheckingAccountNonLocked_thenReturnsTrue() {
            // Act
            boolean isNonLocked = userDetails.isAccountNonLocked();

            // Assert
            assertTrue(isNonLocked, "Cuenta debe estar no bloqueada");
        }

        /**
         * Test: isAccountNonExpired()
         *
         * Verificación:
         * ✅ Retorna true (implementación por defecto)
         */
        @Test
        @DisplayName("isAccountNonExpired() → Retorna true")
        void whenCheckingAccountNonExpired_thenReturnsTrue() {
            // Act
            boolean isNonExpired = userDetails.isAccountNonExpired();

            // Assert
            assertTrue(isNonExpired, "Cuenta debe no estar expirada");
        }

        /**
         * Test: isCredentialsNonExpired()
         *
         * Verificación:
         * ✅ Retorna true (implementación por defecto)
         */
        @Test
        @DisplayName("isCredentialsNonExpired() → Retorna true")
        void whenCheckingCredentialsNonExpired_thenReturnsTrue() {
            // Act
            boolean isNonExpired = userDetails.isCredentialsNonExpired();

            // Assert
            assertTrue(isNonExpired, "Credenciales deben no estar expiradas");
        }

        /**
         * Test: isEnabled()
         *
         * Verificación:
         * ✅ Retorna true (implementación por defecto)
         */
        @Test
        @DisplayName("isEnabled() → Retorna true")
        void whenCheckingEnabled_thenReturnsTrue() {
            // Act
            boolean isEnabled = userDetails.isEnabled();

            // Assert
            assertTrue(isEnabled, "Usuario debe estar habilitado");
        }
    }

    // ==================== GETGETTER TESTS ====================

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        private UserEntity userEntity;
        private UserDetailsImpl userDetails;

        @BeforeEach
        void setUp() {
            userEntity = EntityModelFactory.restaurantOwnerEntity(USER_ID, USER_EMAIL);
            userEntity.setPassword(USER_PASSWORD);
            userEntity.setRole("cliente");
            userDetails = new UserDetailsImpl(userEntity);
        }

        /**
         * Test: getUserEntity() getter
         *
         * Verificación:
         * ✅ Retorna el UserEntity correcto
         */
        @Test
        @DisplayName("getUserEntity() → Retorna UserEntity")
        void whenGettingUserEntity_thenReturnsCorrectEntity() {
            // Act
            UserEntity retrieved = userDetails.getUserEntity();

            // Assert
            assertNotNull(retrieved);
            assertEquals(userEntity, retrieved, "UserEntity debe ser el mismo");
            assertEquals(USER_EMAIL, retrieved.getEmail());
        }
    }
}