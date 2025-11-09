package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.config.security.JwtTokenFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest( USANDO ESTO intenta cargar el JwtUtil y falla
//        controllers = HelloWorldController.class,
//        excludeAutoConfiguration = SecurityAutoConfiguration.class
//
//)
@WebMvcTest(
        controllers = HelloWorldController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,  // Excluye Security
        excludeFilters = @ComponentScan.Filter(                       // Excluye el Filter
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtTokenFilter.class
        )
)
@DisplayName("Tests unitarios para HelloWorldController")
class HelloWorldControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Debe retornar 200 OK con mensaje 'Hola Mundo'")
    void whenCallHello_thenReturnsOk() throws Exception {
        // Arrange
        // (Sin preparación necesaria)

        // Act & Assert
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hola Mundo"));
    }

    @Test
    @DisplayName("Debe retornar 200 OK con contenido correcto")
    void whenCallPublicHello_thenReturnsOk() throws Exception {
        // Arrange
        // (Endpoint público)

        // Act & Assert
        mockMvc.perform(get("/api/public/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Funciona"));
    }
}