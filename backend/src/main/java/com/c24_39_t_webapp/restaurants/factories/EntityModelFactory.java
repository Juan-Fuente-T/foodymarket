package com.c24_39_t_webapp.restaurants.factories;

import com.c24_39_t_webapp.restaurants.dtos.response.ProductSummaryResponseDto;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Product;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.models.UserEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Extensiones a las factories existentes para crear MODELOS (entidades JPA).
 *
 * Las factories originales crean DTOs.
 * Estos métodos crean las entidades reales para tests unitarios que necesitan mocks.
 *
 * Uso en tests:
 * UserEntity owner = EntityModelFactory.userEntity(1L, "owner@test.com");
 * Category category = EntityModelFactory.category(1L, "Pizzas");
 * Restaurant restaurant = EntityModelFactory.restaurant(1L, owner);
 * Product product = EntityModelFactory.product(1L, restaurant, category);
 */
public final class EntityModelFactory {

    private EntityModelFactory() {
    }

    // ================= UserEntity =================

    /**
     * Crea una UserEntity por defecto para tests.
     *
     * @param id    ID del usuario
     * @param email Email del usuario
     * @return nueva instancia de UserEntity
     */
    public static UserEntity userEntity(Long id, String email) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setEmail(email);
        user.setName("Test User");
        user.setRole("RESTAURANTE");
        user.setPassword("hashed_password");
        user.setPhone("123456789");
        user.setAddress("Test Address");
        return user;
    }

    /**
     * Crea una UserEntity con email "CLIENTE".
     */
    public static UserEntity clientEntity(Long id, String email) {
        UserEntity user = userEntity(id, email);
        user.setRole("CLIENTE");
        return user;
    }

    // ================= Category =================

    /**
     * Crea una Category por defecto para tests.
     *
     * @param id   ID de la categoría
     * @param name Nombre de la categoría
     * @return nueva instancia de Category
     */
    public static Category category(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription("Test category: " + name);
        return category;
    }

    /**
     * Crea una Category "Pizzas" con ID 1.
     */
    public static Category defaultCategory() {
        return category(1L, "Pizzas");
    }

    /**
     * Crea una Category "Pastas" con ID 2.
     */
    public static Category pastasCategory() {
        return category(2L, "Pastas");
    }

    // ================= Restaurant =================

    /**
     * Crea un Restaurant por defecto para tests.
     *
     * @param id    ID del restaurante
     * @param owner UserEntity propietario
     * @return nueva instancia de Restaurant
     */
    public static Restaurant restaurant(Long id, UserEntity owner) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setName("Test Restaurant " + id);
        restaurant.setUserEntity(owner);
        restaurant.setDescription("Test restaurant description");
        restaurant.setPhone("123456789");
        restaurant.setEmail("restaurant" + id + "@test.com");
        restaurant.setAddress("Test Address " + id);
        restaurant.setOpeningHours("09:00-23:00");
        restaurant.setLogo("logo.png");
        restaurant.setCoverImage("cover.png");
        return restaurant;
    }

    /**
     * Crea un Restaurant con usuario propietario por defecto.
     */
    public static Restaurant restaurantWithDefaultOwner(Long id) {
        UserEntity owner = userEntity(1L, "owner@test.com");
        return restaurant(id, owner);
    }

    // ================= Product =================

    /**
     * Crea un Product por defecto para tests.
     *
     * @param id         ID del producto
     * @param restaurant Restaurant propietario
     * @param category   Category del producto
     * @return nueva instancia de Product
     */
    public static Product product(Long id, Restaurant restaurant, Category category) {
        Product product = new Product();
        product.setPrd_id(id);
        product.setRestaurant(restaurant);
        product.setCategory(category);
        product.setName("Test Product " + id);
        product.setDescription("Test product description");
        product.setPrice(new BigDecimal("14.99"));
        product.setImage("product.jpg");
        product.setIsActive(true);
        product.setQuantity(50);
        return product;
    }

    /**
     * Crea un Product "Pizza Margherita" con setup completo por defecto.
     */
    public static Product defaultProduct() {
        UserEntity owner = userEntity(1L, "owner@test.com");
        Restaurant restaurant = restaurant(1L, owner);
        Category category = defaultCategory();
        Product product = product(1L, restaurant, category);
        product.setName("Pizza Margherita");
        product.setDescription("Auténtica pizza italiana");
        return product;
    }

    /**
     * Crea un Product con todos los parámetros personalizables.
     */
    public static Product productWithAllData(Long id, String name, String description, BigDecimal price,
                                  Restaurant restaurant, Category category, Integer quantity) {
        Product product = product(id, restaurant, category);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        return product;
    }

    /**
     * Crea un Product inactivo (isActive = false).
     */
    public static Product inactiveProduct(Long id, Restaurant restaurant, Category category) {
        Product product = product(id, restaurant, category);
        product.setIsActive(false);
        return product;
    }

    /**
     * Crea una lista de Products por defecto para tests.
     */
    public static List<Product> defaultProductList() {
        List<Product> list = new ArrayList<>();

        for (long i = 1; i <= 2; i++) {
            Product product = defaultProduct();
            if (i == 2) {
                product = productWithAllData(
                        i,
                        "Pizza Carbonara",
                        "Auténtica carbonara extra",
                        new BigDecimal("9.99"),
                        restaurant(1L, userEntity(1L, "owner@test.com")),
                        category(1L, "Pizzas"),
                        20
                );
            }
            list.add(product);
        }
        return list;
    }
}