package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import com.c24_39_t_webapp.restaurants.dtos.request.LoginRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.AuthResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.UserResponseDto;
import com.c24_39_t_webapp.restaurants.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Tests unitarios para {@link AuthController}.
 * Patrones aplicados:
 * - @WebMvcTest: Solo carga el controlador específico (slice testing). Excluye seguridad y filtros innecesarios.
 * - @MockitoBean: Mockea las dependencias (AuthService).
 * - @Nested: Agrupa tests relacionados para mejor organización.
 * - AAA Pattern: Arrange, Act, Assert en cada test.
 */
@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,  // Excluye Security
        excludeFilters = @ComponentScan.Filter(                       // Excluye el Filter
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@DisplayName("Tests para AuthController")
public class AuthControllerTest {

    /** MockMvc para simular peticiones HTTP al controlador */
    @Autowired
    private MockMvc mockMvc;

    /** Mock del servicio de autenticación */
    @MockitoBean
    private AuthService authService;

    /** ObjectMapper para convertir objetos Java a JSON */
    @Autowired
    private ObjectMapper objectMapper; // Convierte objetos Java ↔ JSON

    /** Constantes para evitar el uso de "magic strings" hardcodeadas en los tests */
    private static final String REGISTER_ENDPOINT = "/api/auth/register";
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "SecurePass123!";
    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

    // ==================== TESTS PARA /api/auth/register ====================

    /**
     * Tests para el endpoint de registro de usuarios.
     * Agrupados en una clase Nested para mejorar la organización
     */
    @Nested
    @DisplayName("Tests para endpoint POST /api/auth/register")
    class RegisterEndpointTests {

        private UserRequestDto validRegisterDto;
        private AuthResponseDto expectedResponse;

        /**
         * Configuación común para los test de registro
         * Crea un UserRequestDto válido y la respuesta esperada
         */
        @BeforeEach
        void setUp() {
            // Arrange común para todos los tests de registro
            validRegisterDto = new UserRequestDto(
                    "Juan Pérez",
                    VALID_EMAIL,
                    "CLIENTE",
                    "555 666 777",
                    "Calle Arriba 11",
                    VALID_PASSWORD
            );
            UserResponseDto userResponseDto = new UserResponseDto(
                    1L,                 // id
                    "Juan Pérez",          // name
                    VALID_EMAIL,           // email
                    "CLIENTE",             // role
                    "555 666 777",         // phone
                    "Calle Arriba 11"      // address
            );

            expectedResponse = new AuthResponseDto(
                    VALID_TOKEN,
                    "Registration successful",
                    userResponseDto
            );
        }

        /**
         * Debug test para listar todos los controller cargados y verificar si AuthController esté entre ellos.
         */
        @Test
        @DisplayName("DEBUG: Ver qué controllers se cargan")
        void debugLoadedControllers() {
            // Esto imprime todos los beans del contexto
            String[] beanNames = mockMvc
                    .getDispatcherServlet()
                    .getWebApplicationContext()
                    .getBeanDefinitionNames();

//            System.out.println("=== BEANS CARGADOS ===");
            for (String bean : beanNames) {
                System.out.println(bean);
            }

            // Busca si AuthController está cargado
            try {
                Object controller = mockMvc
                        .getDispatcherServlet()
                        .getWebApplicationContext()
                        .getBean("authController");
                System.out.println("AuthController encontrado: " + controller.getClass().getName());
            } catch (Exception e) {
                System.out.println("AuthController NO encontrado: " + e.getMessage());
            }
        }
        /**
         * Test para registro exitoso.
         * Verifica que al enviar datos válidos, se recibe un 200 OK con el token esperado.
         * También verifica que el servicio de autenticación fue llamado exactamente una vez.
         */
        @Test
        @DisplayName("Debe retornar 200 OK con token cuando el registro es exitoso")
        void whenRegisterWithValidData_thenReturnsOkWithToken() throws Exception {
            // Arrange
            when(authService.register(any(UserRequestDto.class)))
                    .thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.access_token").value(VALID_TOKEN))
                    .andExpect(jsonPath("$.message").value("Registration successful"));

            // Verify
            verify(authService, times(1)).register(any(UserRequestDto.class));
        }

        /**
         * Test para registro con email inválido.
         * Verifica que al enviar un email inválido, se recibe un 400 Bad Request.
         * También verifica que el servicio de autenticación NUNCA fue llamado.
         */
        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando el email es inválido")
        void whenInvalidEmail_thenReturns400() throws Exception {
            // Arrange
            UserRequestDto invalidDto = new UserRequestDto(
                    "Juan Pérez",
                    "email-sin-arroba",  // Email inválido
                    "CLIENTE",
                    "555 666 777",
                    "Calle Arriba 11",
                    VALID_PASSWORD
            );

            // Act & Assert
            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            // Verificar que el servicio NUNCA fue llamado
            verify(authService, never()).register(any(UserRequestDto.class));
        }
        /**
         * Test para registro con contraseña inválida.
         * Verifica que al enviar una contraseña inválida, se recibe un 400 Bad Request.
         * También verifica que el servicio de autenticación NUNCA fue llamado.
         */
        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando la contraseña tiene menos de 8 caracteres")
        void whenPasswordTooShort_thenReturns400() throws Exception {
            // Arrange
            UserRequestDto invalidDto = new UserRequestDto(
                    "Juan Pérez",
                    "email-sin-arroba",  // Email inválido
                    "CLIENTE",
                    "555 666 777",
                    "Calle Arriba 11",
                    "Short1"
            );

            // Act & Assert
            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            // Verificar que el servicio NUNCA fue llamado
            verify(authService, never()).register(any(UserRequestDto.class));
        }
    }
    /**
     * Tests para el endpoint de login de usuarios.
     * Agrupados en una clase Nested para mejorar la organización
     */
    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpointTests {

        private LoginRequestDto validLoginDto;
        private AuthResponseDto expectedResponse;

        /**
         * Configuación común para los test de login
         * Crea un LoginRequestDto válido y la respuesta esperada
         */
        @BeforeEach
        void setUp() {
            validLoginDto = new LoginRequestDto(VALID_EMAIL, VALID_PASSWORD);

            UserResponseDto userResponseDto = new UserResponseDto(
                    1L,
                    "Juan Pérez",
                    VALID_EMAIL,
                    "CLIENTE",
                    "555 666 777",
                    "Calle Arriba 11"
            );

            expectedResponse = new AuthResponseDto(
                    VALID_TOKEN,
                    "Login successful",
                    userResponseDto
            );
        }

        /**
         * Test para login exitoso.
         * Verifica que al enviar datos válidos, se recibe un 200 OK con el token esperado.
         * También verifica que el servicio de autenticación fue llamado exactamente una vez.
         */
        @Test
        @DisplayName("Debe retornar 200 OK cuando las credenciales son correctas")
        void whenValidCredentials_thenReturnsOkWithToken() throws Exception {
            // Arrange
            when(authService.login(any(LoginRequestDto.class)))
                    .thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(post(LOGIN_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token").value(VALID_TOKEN))
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    .andExpect(jsonPath("$.user.email").value(VALID_EMAIL));

            verify(authService, times(1)).login(any(LoginRequestDto.class));
        }
        /**
         * Test para login con contraseña incorrecta.
         * Verifica que al enviar una contraseña incorrecta, se recibe un 400 Bad Request.
         * También verifica que el servicio de autenticación NUNCA fue llamado.
         */
        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando la contraseña del login no es correcta")
        void whenLoginPasswordIncorrect_thenReturns400() throws Exception {
            // Arrange
            LoginRequestDto loginRequestDto = new LoginRequestDto(
                    VALID_EMAIL,
                    "Short1" //Contraseña muy corta
            );

            // Act & Assert
            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequestDto)))
                    .andExpect(status().isBadRequest());

            // Verificar que el servicio NUNCA fue llamado
            verify(authService, never()).register(any(UserRequestDto.class));
        }

        /**
         * Test para login con email incorrecto.
         * Verifica que al enviar un email inválido, se recibe un 400 Bad Request.
         * También verifica que el servicio de autenticación NUNCA fue llamado.
         */
        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando el email del login no es correcto")
        void whenLoginEmailIncorrect_thenReturns400() throws Exception {
            // Arrange
            LoginRequestDto loginRequestDto = new LoginRequestDto(
                    "email@noexiste.com", // Email erróneo
                    VALID_PASSWORD
            );

            // Act & Assert
            mockMvc.perform(post(REGISTER_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequestDto)))
                    .andExpect(status().isBadRequest());

            // Verificar que el servicio NUNCA fue llamado
            verify(authService, never()).register(any(UserRequestDto.class));
        }
    }
}