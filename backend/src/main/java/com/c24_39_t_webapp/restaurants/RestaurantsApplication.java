package com.c24_39_t_webapp.restaurants;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestaurantsApplication {

    public static void main(String[] args) {
        // Cargar variables .env
//        Dotenv dotenv = Dotenv.configure().load();

        // 1. Mostrar variables DEL ARCHIVO .env (las que lee dotenv)
//        System.out.println("=== Variables del .env ===");
//        System.out.println("JWT_SECRET (desde .env): " + dotenv.get("JWT_SECRET"));
//        System.out.println("SUPABASE_URL (desde .env): " + dotenv.get("SUPABASE_URL"));

//        // 2. Mostrar variables DEL SISTEMA (las que ya están en el entorno)
//        System.out.println("\n=== Variables del sistema ===");
//        System.out.println("JWT_SECRET (System.getenv): " + System.getenv("JWT_SECRET"));
//        System.out.println("SUPABASE_URL (System.getenv): " + System.getenv("SUPABASE_URL"));
//
//        // 3. Mostrar propiedades de Java (las que estableciste con System.setProperty)
//        System.out.println("\n=== Propiedades de Java ===");
//        System.out.println("JWT_SECRET (System.getProperty): " + System.getProperty("JWT_SECRET"));

        SpringApplication.run(RestaurantsApplication.class, args);
    }
}
