package com.c24_39_t_webapp.restaurants;

import com.c24_39_t_webapp.restaurants.repository.UserRepository;
import com.c24_39_t_webapp.restaurants.services.AuthService;
import com.c24_39_t_webapp.restaurants.config.segurity.JwtTokenFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test de integración que verifica el correcto arranque de la aplicación.
 *
 * Este test NO debe ser eliminado porque:
 * 1. Detecta dependencias circulares
 * 2. Verifica que todos los beans se crean correctamente
 * 3. Valida la configuración de Spring Security
 * 4. Asegura que las properties de test son válidas
 *
 * Es el único test con @SpringBootTest, el resto deben usar slice testing
 * (@WebMvcTest, @DataJpaTest, etc.) para mayor velocidad.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Test de arranque de la aplicación")
class RestaurantsApplicationTests {

    /**
     * Smoke test: Verifica que el contexto de Spring Boot se carga sin errores.
     *
     * Este test falla si:
     * - Hay dependencias circulares entre beans
     * - Faltan beans requeridos por @Autowired
     * - La configuración de seguridad está rota
     * - Las properties de application-test.yml son inválidas
     * - Hay errores de sintaxis en @Configuration classes
     */
	@Test
	void contextLoads() {
	}

//Opcional: Test adicional para verificar que beans críticos existen
     @Autowired
     private ApplicationContext context;

     @Test
     void criticalBeansAreLoaded() {
         assertNotNull(context.getBean(AuthService.class));
         assertNotNull(context.getBean(JwtTokenFilter.class));
         assertNotNull(context.getBean(UserRepository.class));
     }

//**Pirámide de Tests (Best Practice)**
//        ```
//           /\
//          /  \
//         /    \
//        /      \
//       /  E2E   \      ← Pocos (5-10%) - @SpringBootTest
//      /----------\
//     /            \
//    / Integration  \   ← Algunos (20-30%) - @DataJpaTest, @SpringBootTest
//   /----------------\
//  /                  \
// /   Unit Tests       \ ← Muchos (60-70%) - @WebMvcTest, Mockito
///______________________\
}
