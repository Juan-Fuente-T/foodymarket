package com.c24_39_t_webapp.restaurants.services.unit;

import com.c24_39_t_webapp.restaurants.config.security.JwtUtil;
import com.c24_39_t_webapp.restaurants.dtos.request.LoginRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.AuthResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
import com.c24_39_t_webapp.restaurants.factories.EntityModelFactory;
import com.c24_39_t_webapp.restaurants.factories.UserFactory;
import com.c24_39_t_webapp.restaurants.models.UserEntity;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
import com.c24_39_t_webapp.restaurants.services.AuthService;
import com.c24_39_t_webapp.restaurants.services.CustomUserDetailService;
import com.c24_39_t_webapp.restaurants.services.IUserService;
import com.c24_39_t_webapp.restaurants.services.impl.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitarios para AuthService y CustomUserDetailService
 *
 * ✅ AuthService.register()
 * ✅ AuthService.login()
 * ✅ CustomUserDetailService.loadUserByUsername()
 *
 * ✅ Users desde EntityModelFactory
 * ✅ DTOs desde UserFactory y loginRequestDto/authResponseDto generados en tests
 * ✅ JwtUtil mockeado
 * ✅ AuthenticationManager mockeado
 *
 * Cobertura:
 * ✅ Registro exitoso
 * ✅ Usuario ya existe → Lanza excepción
 * ✅ Login exitoso
 * ✅ Login con credenciales inválidas → Falla
 * ✅ Cargar usuario por username
 * ✅ Usuario no encontrado → Lanza UsernameNotFoundException
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("AuthService y CustomUserDetailService - Tests")
class AuthServiceAndCustomUserDetailServiceUnitTests {

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "cliente@example.com";
    private static final String USER_ROLE = "CLIENTE";
    private static final String USER_PASSWORD = "SecurePass123!";
    private static final String VALID_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQHRlc3QuY29tIiwicm9sZSI6IkNMSUVOVEUiLCJpZCI6MX0.abc123";

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private IUserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    // ==================== AuthService.register() ====================

    @Nested
    @DisplayName("AuthService.register() - Success Cases")
    class RegisterSuccessCases {

        private UserRequestDto validRequest;
        private UserResponseDto userResponse;

        @BeforeEach
        void setUp() {
            // ✅ Request válido
            validRequest = UserFactory.defaultRequest();

            // ✅ Response del servicio de user
            userResponse = UserFactory.defaultResponse(USER_ID);
        }

        /**
         * Test: Registro exitoso
         *
         * Verificación:
         * ✅ UserRepository.existsByEmail() retorna false
         * ✅ IUserService.createUser() se llamó
         * ✅ JwtUtil.generateToken() se llamó
         * ✅ Retorna AuthResponseDto con token válido
         */
        @Test
        @DisplayName("Registrar nuevo usuario → Exitoso")
        void whenRegisteringNewUser_thenRegistersSuccessfully() {
            // Arrange
            when(userRepository.existsByEmail(USER_EMAIL))
                    .thenReturn(false);
            when(userService.createUser(validRequest))
                    .thenReturn(userResponse);
            when(jwtUtil.generateToken(USER_EMAIL, USER_ROLE, USER_ID))
                    .thenReturn(VALID_JWT_TOKEN);

            // Act
            AuthResponseDto result = authService.register(validRequest);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(VALID_JWT_TOKEN, result.accessToken(), "Token debe coincidir");
            assertEquals(USER_EMAIL, result.user().email(), "Email debe coincidir");
            assertTrue(result.message().toLowerCase().contains("exitosamente"), "Mensaje debe contener 'exitosamente'");

            // Verify
            verify(userRepository, times(1)).existsByEmail(USER_EMAIL);
            verify(userService, times(1)).createUser(validRequest);
            verify(jwtUtil, times(1)).generateToken(USER_EMAIL, USER_ROLE, USER_ID);
        }

        /**
         * Test: Token generado correctamente
         *
         * Verificación:
         * ✅ JwtUtil genera token con datos correctos
         */
        @Test
        @DisplayName("Generación de token → Datos correctos")
        void whenGeneratingToken_thenTokenHasCorrectData() {
            // Arrange
            when(userRepository.existsByEmail(USER_EMAIL))
                    .thenReturn(false);
            when(userService.createUser(validRequest))
                    .thenReturn(userResponse);
            when(jwtUtil.generateToken(USER_EMAIL, USER_ROLE, USER_ID))
                    .thenReturn(VALID_JWT_TOKEN);

            // Act
            AuthResponseDto result = authService.register(validRequest);

            // Assert
            assertNotNull(result.accessToken());
            assertFalse(result.accessToken().isEmpty());

            // Verify
            verify(jwtUtil, times(1)).generateToken(USER_EMAIL, USER_ROLE, USER_ID);
        }
    }

    @Nested
    @DisplayName("AuthService.register() - Error Cases")
    class RegisterErrorCases {

        private UserRequestDto validRequest;

        @BeforeEach
        void setUp() {
            validRequest = UserFactory.defaultRequest();
        }

        /**
         * Test: Usuario ya existe
         *
         * Verificación:
         * ✅ Lanza RuntimeException
         * ✅ IUserService.createUser() NUNCA se llamó
         * ✅ JwtUtil.generateToken() NUNCA se llamó
         */
        @Test
        @DisplayName("Usuario ya existe → Lanza RuntimeException")
        void whenUserAlreadyExists_thenThrowsRuntimeException() {
            // Arrange
            when(userRepository.existsByEmail(USER_EMAIL))
                    .thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> authService.register(validRequest),
                    "Debe lanzar RuntimeException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("ya existe"),
                    "Mensaje: " + exception.getMessage()
            );

            // Verify
            verify(userService, never()).createUser(any());
            verify(jwtUtil, never()).generateToken(any(), any(), any());
        }
    }

    // ==================== AuthService.login() ====================

    @Nested
    @DisplayName("AuthService.login() - Success Cases")
    class LoginSuccessCases {

        private LoginRequestDto loginRequest;
        private UserResponseDto userResponse;
        private Authentication mockAuthentication;

        @BeforeEach
        void setUp() {
            // ✅ Request válido
            loginRequest = new LoginRequestDto(USER_EMAIL, USER_PASSWORD);

            // ✅ Response del servicio
            userResponse = UserFactory.defaultResponse(USER_ID);

            // ✅ Mock Authentication
            mockAuthentication = mock(Authentication.class);
        }

        /**
         * Test: Login exitoso
         *
         * Verificación:
         * ✅ AuthenticationManager.authenticate() se llamó
         * ✅ IUserService.getUserProfile() se llamó
         * ✅ JwtUtil.generateToken() se llamó
         * ✅ Retorna AuthResponseDto con token
         */
        @Test
        @DisplayName("Login válido → Exitoso")
        void whenLoginWithValidCredentials_thenLogsInSuccessfully() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuthentication);
            when(userService.getUserProfile(USER_EMAIL))
                    .thenReturn(userResponse);
            when(jwtUtil.generateToken(USER_EMAIL, USER_ROLE, USER_ID))
                    .thenReturn(VALID_JWT_TOKEN);

            // Act
            AuthResponseDto result = authService.login(loginRequest);

            // Assert
            assertNotNull(result, "Resultado no debe ser null");
            assertEquals(VALID_JWT_TOKEN, result.accessToken(), "Token debe coincidir");
            assertEquals(USER_EMAIL, result.user().email(), "Email debe coincidir");
            assertTrue(result.message().toLowerCase().contains("iniciado sesión"), "Mensaje debe contener 'iniciado sesión'");

            // Verify
            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userService, times(1)).getUserProfile(USER_EMAIL);
            verify(jwtUtil, times(1)).generateToken(USER_EMAIL, USER_ROLE, USER_ID);
        }
    }

    @Nested
    @DisplayName("AuthService.login() - Error Cases")
    class LoginErrorCases {

        private LoginRequestDto loginRequest;

        @BeforeEach
        void setUp() {
            loginRequest = new LoginRequestDto(USER_EMAIL, "WrongPassword");
        }

        /**
         * Test: Credenciales inválidas
         *
         * Verificación:
         * ✅ AuthenticationManager lanza excepción
         */
        @Test
        @DisplayName("Credenciales inválidas → Lanza excepción")
        void whenLoginWithInvalidCredentials_thenThrowsException() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new RuntimeException("Credenciales inválidas"));

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> authService.login(loginRequest),
                    "Debe lanzar excepción por credenciales inválidas"
            );

            // Verify
            verify(userService, never()).getUserProfile(any());
            verify(jwtUtil, never()).generateToken(any(), any(), any());
        }
    }

    // ==================== CustomUserDetailService.loadUserByUsername() ====================

    @Nested
    @DisplayName("CustomUserDetailService.loadUserByUsername() - Success Cases")
    class LoadUserByUsernameSuccessCases {

        private UserEntity user;

        @BeforeEach
        void setUp() {
            // ✅ Usuario desde factory
            user = EntityModelFactory.clientEntity(USER_ID, USER_EMAIL);
        }

        /**
         * Test: Cargar usuario por username (email)
         *
         * Verificación:
         * ✅ UserRepository.findByEmail() se llamó
         * ✅ Retorna UserDetails correcto
         */
        @Test
        @DisplayName("Usuario existe → Retorna UserDetails")
        void whenUserExists_thenReturnsUserDetails() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL))
                    .thenReturn(Optional.of(user));

            // Act
            UserDetails result = customUserDetailService.loadUserByUsername(USER_EMAIL);

            // Assert
            assertNotNull(result, "UserDetails no debe ser null");
            assertEquals(USER_EMAIL, result.getUsername(), "Username debe ser email");
            assertTrue(result.isEnabled(), "Usuario debe estar habilitado");

            // Verify
            verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        }

        /**
         * Test: UserDetails contiene información correcta
         *
         * Verificación:
         * ✅ Datos del usuario se mapean correctamente a UserDetails
         */
        @Test
        @DisplayName("Datos del usuario → Se mapean correctamente")
        void whenLoadingUser_thenDataMapsCorrectly() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL))
                    .thenReturn(Optional.of(user));

            // Act
            UserDetails result = customUserDetailService.loadUserByUsername(USER_EMAIL);

            // Assert
            assertNotNull(result);
            assertEquals(USER_EMAIL, result.getUsername());
            // UserDetailsImpl debe contener el UserEntity
            assertTrue(result instanceof UserDetailsImpl, "Debe ser instancia de UserDetailsImpl");

            // Verify
            verify(userRepository, times(1)).findByEmail(USER_EMAIL);
        }
    }

    @Nested
    @DisplayName("CustomUserDetailService.loadUserByUsername() - Error Cases")
    class LoadUserByUsernameErrorCases {

        /**
         * Test: Usuario no encontrado
         *
         * Verificación:
         * ✅ Lanza UsernameNotFoundException
         */
        @Test
        @DisplayName("Usuario no encontrado → Lanza UsernameNotFoundException")
        void whenUserNotFound_thenThrowsUsernameNotFoundException() {
            // Arrange
            when(userRepository.findByEmail("nonexistent@test.com"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            UsernameNotFoundException exception = assertThrows(
                    UsernameNotFoundException.class,
                    () -> customUserDetailService.loadUserByUsername("nonexistent@test.com"),
                    "Debe lanzar UsernameNotFoundException"
            );

            assertTrue(
                    exception.getMessage().toLowerCase().contains("no encontrado"),
                    "Mensaje: " + exception.getMessage()
            );

            // Verify
            verify(userRepository, times(1)).findByEmail("nonexistent@test.com");
        }

        /**
         * Test: Email null
         *
         * Verificación:
         * ✅ Lanza excepción
         */
        @Test
        @DisplayName("Email null → Lanza excepción")
        void whenEmailIsNull_thenThrowsException() {
            // Arrange
            when(userRepository.findByEmail(null))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(Exception.class,
                    () -> customUserDetailService.loadUserByUsername(null)
            );

            // Verify
            verify(userRepository, times(1)).findByEmail(null);
        }
    }
}