package com.c24_39_t_webapp.restaurants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;


@SpringBootApplication
public class RestaurantsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantsApplication.class, args);
    }
//    @Component
//    static class PropertyTester implements CommandLineRunner {
//
//        // Aquí SÍ puedes inyectar el valor.
//        // Spring lo inyectará DESPUÉS de leer application-prod.properties
//        @Value("${JWT_SECRET}")
//        private String miSecretoJwt;
//
//        @Override
//        public void run(String... args) throws Exception {
//            System.out.println("======================================================");
//            System.out.println("===> EL SECRETO JWT LEÍDO POR SPRING ES: " + miSecretoJwt);
//            System.out.println("======================================================");
//
//            // Ahora comprobamos tu 'System.getenv()'
//            String envVar = System.getenv("JWT_SECRET");
//            System.out.println("===> El System.getenv(\"JWT_SECRET\") dice: " + envVar);
//        }
//    }
}

