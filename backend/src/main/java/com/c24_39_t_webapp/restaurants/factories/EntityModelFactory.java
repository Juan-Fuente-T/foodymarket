package com.c24_39_t_webapp.restaurants.factories;

import com.c24_39_t_webapp.restaurants.dtos.request.*;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductSummaryResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.RestaurantResponseDto;
import com.c24_39_t_webapp.restaurants.models.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Extensiones a las factories existentes para crear MODELOS (entidades JPA).
 * <p>
 * Las factories originales crean DTOs.
 * Estos métodos crean las entidades reales para tests unitarios que necesitan mocks.
 * <p>
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
     * Crea UserEntity SINCRONIZADA con UserFactory.defaultRequest().
     *
     * Valores consultados (NO hardcodeados):
     * - name: "Juan Pérez" desde UserFactory.defaultRequest()
     * - role: "CLIENTE" desde UserFactory.defaultRequest()
     * - phone: "1234567890" desde UserFactory.defaultRequest()
     * - address: "Calle de arriba 99" desde UserFactory.defaultRequest()
     * - password: "password123" desde UserFactory.defaultRequest()
     *
     * @param id    ID del usuario
     * @param email Email personalizado (override)
     * @return nueva instancia de UserEntity
     */
    public static UserEntity userEntity(Long id, String email) {
        UserRequestDto dto = UserFactory.defaultRequest();

        UserEntity user = new UserEntity();
        if (id != null) {
            user.setId(id);  // Solo se asigna si no es null
        }
        user.setEmail(email);  // Override del email
        user.setName(dto.name());              // "Juan Pérez" - desde factory
        user.setRole(dto.role());              // "CLIENTE" - desde factory
        user.setPassword(dto.password());      // "password123" - desde factory
        user.setPhone(dto.phone());            // "1234567890" - desde factory
        user.setAddress(dto.address());        // "Calle de arriba 99" - desde factory
        return user;
    }

    /**
     * Crea UserEntity con rol CLIENTE explícito.
     */
    public static UserEntity clientEntity(Long id, String email) {
        UserEntity user = userEntity(id, email);
        user.setRole("CLIENTE");
        return user;
    }

    /**
     * Crea UserEntity con rol RESTAURANTE explícito.
     */
    public static UserEntity restaurantOwnerEntity(Long id, String email) {
        UserEntity user = userEntity(id, email);
        user.setRole("RESTAURANTE");
        return user;
    }

    // ================= Category =================

    /**
     * Crea Category SINCRONIZADA con CategoryFactory.defaultRequest().
     *
     * Valores consultados (NO hardcodeados):
     * - name: "Pizzas" desde CategoryFactory.defaultRequest()
     * - description: "Deliciosas pizzas artesanales" desde CategoryFactory.defaultRequest()
     *
     * @param id ID de la categoría
     * @return nueva instancia de Category
     */
    public static Category category(Long id) {
        CategoryRequestDto dto = CategoryFactory.defaultRequest();

        Category category = new Category();
        category.setId(id);
        category.setName(dto.name());              // "Pizzas" - desde factory
        category.setDescription(dto.description()); // "Deliciosas pizzas artesanales" - desde factory
        return category;
    }
    /**
     * Crea Category con nombre personalizado.
     */
    public static Category category(Long id, String name) {
        Category cat = category(id);
        cat.setName(name);
        return cat;
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
     * Crea Restaurant SINCRONIZADA con RestaurantFactory.defaultRequest().
     *
     * Valores consultados (NO hardcodeados):
     * - name: "Atlántico" desde RestaurantFactory.defaultRequest()
     * - description: "Comida fresca del mar" desde RestaurantFactory.defaultRequest()
     * - phone: "666234123" desde RestaurantFactory.defaultRequest()
     * - address: "La calle de enmedio 45" desde RestaurantFactory.defaultRequest()
     * - openingHours: "12:30-23:00" desde RestaurantFactory.defaultRequest()
     *
     * @param id    ID del restaurante
     * @param owner UserEntity propietario
     * @return nueva instancia de Restaurant
     */
    public static Restaurant restaurant(Long id, UserEntity owner) {
        RestaurantRequestDto dto = RestaurantFactory.defaultRequest(owner.getId(), owner.getEmail());
//        RestaurantRequestDto requestDto = RestaurantFactory.defaultRequest(owner.getId(), owner.getEmail());
//        RestaurantResponseDto dto = RestaurantFactory.responseFromRequest(requestDto, 1L);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setUserEntity(owner);
        restaurant.setName(dto.name());                      // "Atlántico" - desde factory
        restaurant.setDescription(dto.description());        // "Comida fresca del mar" - desde factory
        restaurant.setPhone(dto.phone());                    // "666234123" - desde factory
        restaurant.setEmail(dto.email());                    // email del owner - desde factory
        restaurant.setAddress(dto.address());                // "La calle de enmedio 45" - desde factory
        restaurant.setOpeningHours(dto.openingHours());      // "12:30-23:00" - desde factory
        restaurant.setLogo(dto.logo());                      // null - desde factory
        restaurant.setCoverImage(dto.coverImage());          // null - desde factory
        return restaurant;
    }

    /**
     * Crea un Restaurant con usuario propietario por defecto.
     */
//    public static Restaurant restaurantWithDefaultOwner(Long id) {
//        UserEntity owner = userEntity(1L, "owner@test.com");
//        return restaurant(id, owner);
//    }

    public static RestaurantCuisine restaurantCuisine(Long cuisineId, String cuisineName) {
        RestaurantCuisine newCuisine = new RestaurantCuisine();
        newCuisine.setId(cuisineId);
        newCuisine.setName(cuisineName);
        return newCuisine;
    }


    // ================= Product =================

    /**
     * Crea Product SINCRONIZADA con ProductFactory.defaultProductRequest().
     *
     * GARANTÍA: Los valores son IDÉNTICOS a los que devuelve ProductFactory.defaultProductRequest()
     *
     * Valores consultados (NO hardcodeados):
     * - name: "Pizza Margherita" desde ProductFactory.defaultProductRequest()
     * - description: "Auténtica pizza italiana" desde ProductFactory.defaultProductRequest()
     * - price: 14.99 desde ProductFactory.defaultProductRequest()
     * - image: "https://example.com/pizza-mejorada.jpg" desde ProductFactory.defaultProductRequest()
     * - isActive: true desde ProductFactory.defaultProductRequest()
     * - quantity: 60 desde ProductFactory.defaultProductRequest()
     *
     * @param productId  ID del producto
     * @param restaurant Restaurant propietario
     * @param category   Category del producto
     * @return nueva instancia de Product
     */
    public static Product product(Long productId, Restaurant restaurant, Category category) {
        ProductRequestDto dto = ProductFactory.defaultProductRequest(restaurant.getId());

        Product product = new Product();
        product.setPrd_id(productId);
        product.setRestaurant(restaurant);
        product.setCategory(category);
        product.setName(dto.name());                         // "Pizza Margherita" - desde factory
        product.setDescription(dto.description());           // "Auténtica pizza italiana" - desde factory
        product.setPrice(dto.price());                       // 14.99 - desde factory
        product.setImage(dto.image());                       // "https://example.com/pizza-mejorada.jpg" - desde factory
        product.setIsActive(dto.isActive() != null ? dto.isActive() : true);
        product.setQuantity(dto.quantity());                 // 60 - desde factory
        return product;
    }
    /**
     * Crea Product con valores PERSONALIZABLES (no sincronizado con factory).
     *
     * @param productId  ID del producto
     * @param name       Nombre personalizado
     * @param description Descripción personalizada
     * @param price      Precio personalizado
     * @param restaurant Restaurant propietario
     * @param category   Category del producto
     * @param quantity   Cantidad personalizada
     * @param isActive   Estado personalizado
     * @return nueva instancia de Product
     */
//    public static Product product(Long productId, String name, String description, BigDecimal price,
//                                  Restaurant restaurant, Category category, Integer quantity, Boolean isActive) {
//        Product product = new Product();
//        product.setPrd_id(productId);
//        product.setRestaurant(restaurant);
//        product.setCategory(category);
//        product.setName(name);
//        product.setDescription(description);
//        product.setPrice(price);
//        product.setImage("https://example.com/product.jpg");
//        product.setIsActive(isActive != null ? isActive : true);
//        product.setQuantity(quantity != null ? quantity : 50);
//        return product;
//    }

    /**
     * Crea un Product "Pizza Margherita" con setup completo por defecto.
     * USA EXCLUSIVAMENTE métodos existentes que llaman a las factories.
     */
    public static Product defaultProduct() {
        UserEntity owner = userEntity(1L, "owner@test.com");
        Restaurant restaurant = restaurant(1L, owner);
        Category category = defaultCategory();
        return product(1L, restaurant, category);
    }
    /**
     * Crea Product para UPDATE SINCRONIZADA con ProductFactory.defaultUpdatedProduct().
     *
     * GARANTÍA: Los valores son IDÉNTICOS a los que devuelve ProductFactory.defaultUpdatedProduct()
     *
     * @param productId  ID del producto
     * @param restaurant Restaurant propietario
     * @param category   Category del producto
     * @return nueva instancia de Product
     */
//    public static Product productForUpdate(Long productId, Restaurant restaurant, Category category) {
//        ProductUpdateDto dto = ProductFactory.defaultUpdatedProduct(restaurant.getId(), "Pizza Margherita", "Auténtica pizza italiana");
//
//        Product product = new Product();
//        product.setPrd_id(productId);
//        product.setRestaurant(restaurant);
//        product.setCategory(category);
//        product.setName(dto.name());                         // "Pizza Margherita" - desde factory
//        product.setDescription(dto.description());           // "Auténtica pizza italiana" - desde factory
//        product.setPrice(dto.price());                       // 14.99 - desde factory
//        product.setImage(dto.image());                       // "https://example.com/pizza-mejorada.jpg" - desde factory
//        product.setIsActive(dto.isActive() != null ? dto.isActive() : true);
//        product.setQuantity(dto.quantity());                 // 60 - desde factory
//        return product;
//    }
    /**
     * Crea un Product con todos los parámetros personalizables.
     */
//    public static Product productWithAllData(Long id, String name, String description, BigDecimal price,
//                                             Restaurant restaurant, Category category, Integer quantity) {
//        Product product = product(id, restaurant, category);
//        product.setName(name);
//        product.setDescription(description);
//        product.setPrice(price);
//        product.setQuantity(quantity);
//        return product;
//    }

    /**
     * Crea un Product inactivo (isActive = false).
     */
//    public static Product inactiveProduct(Long id, Restaurant restaurant, Category category) {
//        Product product = product(id, restaurant, category);
//        product.setIsActive(false);
//        return product;
//    }

    /**
     * Crea una lista de Products por defecto para tests.
     */
    public static List<Product> defaultProductList() {
        List<Product> list = new ArrayList<>();

        // Usar defaultProduct() para el primer producto
        list.add(defaultProduct());

        // Segundo producto, usando los métodos existentes que usan factories
        UserEntity owner = userEntity(1L, "owner@test.com");
        Restaurant restaurant = restaurant(1L, owner);
        Category category = defaultCategory();

        Product secondProduct = product(
                2L,
                restaurant,
                category
        );
        // Si se necesita cambiar valores específicos, se hace después de crear el producto base
        secondProduct.setName("Pizza Carbonara");
        secondProduct.setDescription("Auténtica carbonara extra");
        secondProduct.setPrice(new BigDecimal("9.99"));
        secondProduct.setQuantity(20);

        list.add(secondProduct);
        return list;
    }

//    /**
//     * Crea lista de productos para búsquedas por nombre - USANDO MÉTODOS EXISTENTES
//     */
//    public static List<Product> productsForNameSearch() {
//        List<Product> list = new ArrayList<>();
//        UserEntity owner = userEntity(1L, "owner@test.com");
//        Restaurant restaurant = restaurant(1L, owner);
//        Category category = defaultCategory();
//
//        // Usar productWithAllData que a su vez llama a product() base
//        list.add(productWithAllData(1L, "Pizza Margherita", "Clásica italiana",
//                new BigDecimal("12.99"), restaurant, category, 25));
//        list.add(productWithAllData(2L, "Pizza Carbonara", "Carbonara auténtica",
//                new BigDecimal("14.99"), restaurant, category, 20));
//        list.add(productWithAllData(3L, "Pizza Barbacoa", "Barbacoa especial",
//                new BigDecimal("15.99"), restaurant, category, 15));
//
//        return list;
//    }

// ================= Order =================

        /**
         * Crea Order SINCRONIZADA con OrderFactory.defaultRequest().
         *
         * @param orderId      ID de la orden
         * @param client       UserEntity cliente (debe ser CLIENTE role)
         * @param restaurant   Restaurant propietario
         * @return nueva instancia de Order
         */
        public static Order order(Long orderId, UserEntity client, Restaurant restaurant) {
            Order order = new Order();
            order.setOrd_id(orderId);
            order.setClientId(client);
            order.setRestaurantId(restaurant);
            order.setStatus(OrderStatus.pendiente);
            order.setTotal(new BigDecimal("29.98"));
            order.setComments("Sin instrucciones especiales");
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            order.setDetails(new ArrayList<>());
            return order;
        }

        /**
         * Crea Order con valores PERSONALIZABLES.
         */
        public static Order order(Long orderId, UserEntity client, Restaurant restaurant,
                OrderStatus status, BigDecimal total, String comments) {
            Order order = order(orderId, client, restaurant);
            order.setStatus(status);
            order.setTotal(total);
            order.setComments(comments);
            return order;
        }

        /**
         * Crea una Order por defecto (con cliente y restaurante por defecto).
         */
//        public static Order defaultOrder() {
//            UserEntity client = clientEntity(1L, "client@test.com");
//            UserEntity owner = restaurantOwnerEntity(2L, "owner@test.com");
//            Restaurant restaurant = restaurant(1L, owner);
//            return order(1L, client, restaurant);
//        }

        // ================= OrderDetails =================

        /**
         * Crea OrderDetails SINCRONIZADO.
         *
         * @param detailId ID del detalle
         * @param order    Order asociada
         * @param product  Product asociado
         * @param quantity Cantidad
         * @param subtotal Subtotal
         * @return nueva instancia de OrderDetails
         */
//        public static OrderDetails orderDetails(Long detailId, Order order, Product product,
//                Integer quantity, BigDecimal subtotal) {
//            OrderDetails detail = new OrderDetails();
//            detail.setOdt_id(detailId);
//            detail.setOrder(order);
//            detail.setProduct(product);
//            detail.setQuantity(quantity);
//            detail.setSubtotal(subtotal);
//            return detail;
//        }
    // ================= Review =================

    /**
     * Crea Review desde factory.
     *
     * @param reviewId ID de la reseña
     * @param user     UserEntity que crea la reseña
     * @param restaurant Restaurant sobre el que es la reseña
     * @param score    Puntuación (0-10)
     * @param comments Comentario
     * @return nueva instancia de Review
     */
    public static Review review(Long reviewId, UserEntity user, Restaurant restaurant, Integer score, String comments) {
        Review review = new Review();
        review.setId(reviewId);
        review.setUser(user);
        review.setRestaurant(restaurant);
        review.setScore(score);
        review.setComments(comments);
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }

    /**
     * Crea una Review por defecto.
     */
//    public static Review defaultReview() {
//        UserEntity user = clientEntity(1L, "user@test.com");
//        UserEntity owner = restaurantOwnerEntity(2L, "owner@test.com");
//        Restaurant restaurant = restaurant(1L, owner);
//        return review(1L, user, restaurant, 8, "Excelente");
//    }

    // ================= UserEntity (Auth-specific) =================

    /**
     * Crea UserEntity con contraseña SIN encodificar (para testing de login).
     *
     * @param id       ID del usuario
     * @param email    Email personalizado
     * @param password Contraseña SIN encodificar
     * @return nueva instancia de UserEntity
     */
//    public static UserEntity userEntityWithPassword(Long id, String email, String password) {
//        UserEntity user = new UserEntity();
//        user.setId(id);
//        user.setEmail(email);
//        user.setName("Test User");
//        user.setRole("CLIENTE");
//        user.setPhone("555555555");
//        user.setAddress("Test Address 123");
//        user.setPassword(password);  // ← Sin codificar (mockeamos passwordEncoder en tests)
//        user.setCreatedAt(LocalDateTime.now());
//        return user;
//    }

    /**
     * Crea UserEntity por defecto con contraseña.
     */
//    public static UserEntity defaultUserWithPassword() {
//        return userEntityWithPassword(1L, "user@test.com", "SecurePass123!");
//    }

    /**
     * Crea UserEntity RESTAURANTE con contraseña.
     */
//    public static UserEntity restaurantOwnerEntityWithPassword(Long id, String email, String password) {
//        UserEntity user = new UserEntity();
//        user.setId(id);
//        user.setEmail(email);
//        user.setName("Restaurant Owner");
//        user.setRole("RESTAURANTE");
//        user.setPhone("666666666");
//        user.setAddress("Restaurant Address 456");
//        user.setPassword(password);
//        user.setCreatedAt(LocalDateTime.now());
//        return user;
//    }
}