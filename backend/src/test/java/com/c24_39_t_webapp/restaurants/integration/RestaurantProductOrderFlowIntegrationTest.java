package com.c24_39_t_webapp.restaurants.integration;

import com.c24_39_t_webapp.restaurants.dtos.request.ProductRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.RestaurantRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.UserRequestDto;
import com.c24_39_t_webapp.restaurants.factories.*;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.repository.CategoryRepository;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.c24_39_t_webapp.restaurants.integration.support.LoginRequest;

/**
 * INTEGRATION TEST: Restaurant → Product → Order
 * <p>
 * ✅ Flujo crítico de negocio:
 * 1. RESTAURANTE se registra + crea restaurante
 * 2. RESTAURANTE crea producto
 * 3. CLIENTE se registra
 * 4. CLIENTE crea orden del producto
 * <p>
 * ✅ Verifica cascadas de relaciones BD
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@Sql(scripts = "/tipologias-data.sql") //Trae las tipologías necesarias desde un archivo SQL
@DisplayName("Integration Test - Restaurant Product Order Flow")
class RestaurantProductOrderFlowIntegrationTest {

    private static final String REGISTER_ENDPOINT = "/api/auth/register";
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String RESTAURANT_ENDPOINT = "/api/restaurant";
    private static final String PRODUCT_ENDPOINT = "/api/product";
    private static final String ORDER_ENDPOINT = "/api/order";
    private static final Long RESTAURANT_ID = 1L;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;


    /**
     * ✅ FLUJO COMPLETO: Restaurant → Product → Order
     * <p>
     * 1. RESTAURANTE: Register
     * 2. RESTAURANTE: Login + obtener token
     * 3. RESTAURANTE: Crear restaurante
     * 4. RESTAURANTE: Crear producto
     * 5. CLIENTE: Register
     * 6. CLIENTE: Login + obtener token
     * 7. CLIENTE: Crear orden con ese producto
     */
    @Test
    @DisplayName("FLUJO: Restaurant crea Product → Client crea Order (cascadas correctas)")
    void whenRestaurantCreatesProductAndClientCreatesOrder_thenCascadesWork() throws Exception {
        long timestamp = System.currentTimeMillis();
        String restaurantEmail = "restaurante-" + timestamp + "@example.com";
        String clientEmail = "cliente-" + timestamp + "@example.com";

        log.info("=== INICIO: Restaurant → Product → Order Flow ===");

        // 1️⃣ USUARIO: REGISTER
        UserRequestDto userRegisterDto = UserFactory.requestWith(
                "Atlántico",
                restaurantEmail,
                "RESTAURANTE",//Debe ser role RESTAURANTE
                "555 666 777",
                "Calle Arriba 11",
                "RestaurantPass123");

        String userRegisterPayload = objectMapper.writeValueAsString(userRegisterDto);

        mockMvc.perform(
                post(REGISTER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRegisterPayload))
                //  .andExpect(status().isCreated())
                .andExpect(status().isOk())
                .andDo(result -> log.info("✅ Step 1: USUARIO registrado"));

        // 2️⃣ USER: LOGIN
        String userLoginPayload = objectMapper.writeValueAsString(
                new LoginRequest(restaurantEmail, "RestaurantPass123")
        );

        String[] restaurantTokenHolder = new String[1];

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userLoginPayload))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    restaurantTokenHolder[0] = objectMapper.readTree(responseBody).get("access_token").asText();
                    log.info("✅ Step 2: USER token obtenido");
                });

        // 3️⃣ RESTAURANTE: CREAR RESTAURANTE
        RestaurantRequestDto restaurantRegisterDto = RestaurantFactory.defaultRequest(RESTAURANT_ID, restaurantEmail);

        String restaurantCreatePayload = objectMapper.writeValueAsString(restaurantRegisterDto);

        Long[] restaurantIdHolder = new Long[1];

        mockMvc.perform(post(RESTAURANT_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + restaurantTokenHolder[0])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(restaurantCreatePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rst_id").exists())
                .andDo(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    restaurantIdHolder[0] = objectMapper.readTree(responseBody).get("rst_id").asLong();
                    log.info("✅ Step 3: RESTAURANTE creado con ID: {}", restaurantIdHolder[0]);
                })
                .andDo(print());

        // 4️⃣ RESTAURANTE: CREAR CATEGORÍA + PRODUCTO
        Category category = new Category();
        category.setName("Pizzas");
        category.setDescription("Pizzas caseras");
        categoryRepository.save(category);

        ProductRequestDto productRegisterDto = ProductFactory.defaultProductRequest(1L);

        String productCreatePayload = objectMapper.writeValueAsString(productRegisterDto);

        Long[] productIdHolder = new Long[1];

        mockMvc.perform(post(PRODUCT_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + restaurantTokenHolder[0])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productCreatePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prd_id").exists())
                .andDo(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    productIdHolder[0] = objectMapper.readTree(responseBody).get("prd_id").asLong();
                    log.info("✅ Step 4: PRODUCTO creado con ID: {}", productIdHolder[0]);
                });

        // 5️⃣ CLIENTE: REGISTER
        UserRequestDto clientRegisterDto = UserFactory.requestWith(
                "Cliente Test",
//                "cliente@test.com",
                clientEmail,
                "CLIENTE",
                "555333333",
                "Client Street",
                "ClientPass123!"
        );
        String clientRegisterPayload = objectMapper.writeValueAsString(clientRegisterDto);

        Long[] clientIdHolder = new Long[1];

        mockMvc.perform(post(REGISTER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientRegisterPayload))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    clientIdHolder[0] = objectMapper.readTree(responseBody).get("user").get("id").asLong();  // ← CAPTURA EL ID
                    log.info("✅ Step 5: CLIENTE registrado con ID: {}", clientIdHolder[0]);
                });

        // 6️⃣ CLIENTE: LOGIN
        String clientLoginPayload = objectMapper.writeValueAsString(
                new LoginRequest(clientEmail, "ClientPass123!")
        );

        String[] clientTokenHolder = new String[1];

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientLoginPayload))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    clientTokenHolder[0] = objectMapper.readTree(responseBody).get("access_token").asText();
                    log.info("✅ Step 6: CLIENTE token obtenido");
                });

        // 7️⃣ CLIENTE: CREAR ORDEN
        String orderCreatePayload = objectMapper.writeValueAsString(
                OrderFactory.defaultRequest(restaurantIdHolder[0], clientIdHolder[0] )
        );

        mockMvc.perform(post(ORDER_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientTokenHolder[0])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderCreatePayload)
                        .param("email", clientEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ord_Id").exists())
                .andDo(result -> log.info("✅ Step 7: ORDEN creada correctamente"));

        log.info("=== FIN: Flujo Restaurant → Product → Order completado ===\n");
    }
}