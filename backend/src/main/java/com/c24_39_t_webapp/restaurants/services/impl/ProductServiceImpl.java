package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.response.GroupedProductsResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductSummaryResponseDto;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.ProductNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Product;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.repository.CategoryRepository;
import com.c24_39_t_webapp.restaurants.repository.ProductRepository;
import com.c24_39_t_webapp.restaurants.repository.RestaurantRepository;
import com.c24_39_t_webapp.restaurants.repository.UserRepository;
import com.c24_39_t_webapp.restaurants.services.IProductService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.TreeMap;

@Slf4j
@Service
@AllArgsConstructor
public class ProductServiceImpl implements IProductService {
    private final ProductRepository productRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public ProductResponseDto addProduct(Product product){
        log.info("Intentando crear un producto para el restaurante con ID: {}", product.getRestaurant().getId());
//        if (restaurantId == null || restaurantId <= 0) {
//            throw new IllegalArgumentException("El ID del restaurante no es válido");
//        }
        Restaurant restaurant = restaurantRepository.findById(product.getRestaurant().getId())
                .orElseThrow(() -> new RestaurantNotFoundException("No se ha encontrado el restaurante"));
        Category category = categoryRepository.findById(product.getCategory().getCtg_id())
                .orElseThrow(() -> new CategoryNotFoundException("No se ha encontrado la categoria"));
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Recuperando el email del usuario de la autenticacion: {}", userEmail);
        log.info("Recuperando el email del usuario del restaurante: {}", restaurant.getUserEntity().getEmail());
        log.info("Usuario autenticado con email: {}", userEmail);
        if (!restaurant.getUserEntity().getEmail().equals(userEmail)) {
            throw new SecurityException("No tienes permiso para añadir productos a este restaurante");
        }

        product.setRestaurant(restaurant);
        product.setCategory(category);

        product = productRepository.save(product);
        log.info("Producto creado con éxito");

        return new ProductResponseDto(
                product.getPrd_id(),
                product.getRestaurant().getId(),
                product.getCategory().getCtg_id(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImage(),
                product.getIsActive(),
                product.getQuantity()
        );
    }

    @Override
    public List<ProductResponseDto> findAllProducts() {
        log.info("Recuperando todos los productos.");
        List<Product> products = productRepository.findAll();

        if (products.isEmpty()) {
            throw new ProductNotFoundException("No se encontraron productos.");
        }
        return products.stream()
                .map(product -> new ProductResponseDto(
                        product.getPrd_id(),
                        product.getRestaurant().getId(),
                        product.getCategory().getCtg_id(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getImage(),
                        product.getIsActive(),
                        product.getQuantity()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDto findProductById(Long prd_id) {
        log.info("Buscando el product con ID: {}", prd_id);
        if (prd_id == null || prd_id <= 0) {
            log.warn("El ID del producto proporcionado es invalido: {}", prd_id);
            throw new ProductNotFoundException("El ID del producto no es válido " + prd_id);
        }
        log.info("Buscando el product con ID: {}", prd_id);
        return productRepository.findById(prd_id)
                .map(product -> new ProductResponseDto(
                        product.getPrd_id(),
                        product.getRestaurant().getId(),
                        product.getCategory().getCtg_id(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getImage(),
                        product.getIsActive(),
                        product.getQuantity()
                ))
                .orElseThrow(() -> {
                    log.warn("No se encontro un producto con el ID: {}", prd_id);
                    return new ProductNotFoundException("No se encontro una producto con ese ID: " + prd_id);
                });
    }

    @Transactional
    @Override
    public ProductResponseDto updateProduct(Product product) {
        log.info("Actualizando el producto con ID {}", product.getPrd_id());

        Product updatedProduct = productRepository.saveAndFlush(product);
        log.info("Producto con ID {} ha sido actualizado con éxito", product.getPrd_id());

        return new ProductResponseDto(
                updatedProduct.getPrd_id(),
                updatedProduct.getRestaurant().getId(),
                updatedProduct.getCategory().getCtg_id(),
                updatedProduct.getName(),
                updatedProduct.getDescription(),
                updatedProduct.getPrice(),
                updatedProduct.getImage(),
                updatedProduct.getIsActive(),
                updatedProduct.getQuantity()
        );
    }

    @Override
    @Transactional
    public void deleteProduct(Long prd_id) {
        if (!productRepository.existsById(prd_id)) {
            throw new ProductNotFoundException("Product no encontrado con id: " + prd_id);
        }
        productRepository.deleteById(prd_id);
    }

    @Override
//    public List<ProductSummaryResponseDto> findProductsByCategory(Long ctg_Id) {
//        log.info("Recuperando los producto de la categoria con ID {}", ctg_Id);
//        List<Product> products = productRepository.findProductsByCategory(ctg_Id);
//
//        if (products.isEmpty()) {
//            throw new ProductNotFoundException("No se encontraron productos para la categoría con id: " + ctg_Id);
    public List<ProductSummaryResponseDto> findProductsByCategory(Category category) {
        log.info("Recuperando los producto de la categoria con ID {}", category);
        List<Product> products = productRepository.findProductsByCategory(category);

        if (products.isEmpty()) {
            throw new ProductNotFoundException("No se encontraron productos para la categoría con id: " + category);
        }

        return products.stream()
                .map(product -> new ProductSummaryResponseDto(
                        product.getPrd_id(),
                        product.getRestaurant().getId(),
                        product.getCategory().getCtg_id(),
                        product.getName(),
                        product.getDescription(),
                        product.getImage()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductSummaryResponseDto> findProductsByName(String name) {
        log.info("Buscando el product con ID: {}", name);
        if (name == null || name.trim().isEmpty()) {
            log.warn("El nombre del producto proporcionado no es valido: {}", name);
            throw new IllegalArgumentException("El nombre del producto no puede ser nulo o estar vacío");
        }
        name = name.trim();
        if (name.length() < 2) {
            log.warn("El nombre del producto es demasiado corto: {}", name);
            throw new IllegalArgumentException("El nombre del producto debe tener al menos 2 caracteres");
        }
        return productRepository.findProductsByName(name)
                .stream()
                .map(product -> new ProductSummaryResponseDto(
                        product.getPrd_id(),
                        product.getRestaurant().getId(),
                        product.getCategory().getCtg_id(),
                        product.getName(),
                        product.getDescription(),
                        product.getImage()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> findProductsByRestaurantId(Long restaurantId) {
        log.info("Buscando productos del restaurante con ID: {}", restaurantId);
//        if (newRestaurant == null || newRestaurant <= 0) {
//            log.warn("El ID del restaurante proporcionado es invalido: {}", restaurantId);
//            throw new ProductNotFoundException("El ID del restaurante no es válido " + restaurantId);
//        }
//        Restaurant newRestaurant = restaurantRepository.findById(restaurant.rst_id());

        List<Product> products = productRepository.findProductsByRestaurantId(restaurantId);
        return products.stream()
                .map(product -> new ProductResponseDto(
                        product.getPrd_id(),
                        product.getRestaurant().getId(),
                        product.getCategory().getCtg_id(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getImage(),
                        product.getIsActive(),
                        product.getQuantity()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupedProductsResponseDto> findProductsByRestaurantIdAndCategory(Long restaurantId) {
        log.info("Buscando productos del restaurante con ID: {}", restaurantId);

            // Obtener los productos del repositorio
            List<Product> products = productRepository.findProductsByRestaurantIdAndCategory(restaurantId);

            // Agrupar los productos por categoría y restaurante
            Map<String, Map<Long, List<ProductResponseDto>>> groupedProducts = products.stream()
                    .collect(Collectors.groupingBy(
                            product -> product.getCategory().getName(), // Agrupar por nombre de categoría
                            TreeMap::new, // Ordenar por nombre de categoría
                            Collectors.groupingBy(
                                    product -> product.getRestaurant().getId(), // Agrupar por ID del restaurante
                                    TreeMap::new, // Ordenar por ID del restaurante
                                    Collectors.mapping(
                                            product -> new ProductResponseDto(
                                                    product.getPrd_id(),
                                                    product.getRestaurant().getId(),
                                                    product.getCategory().getCtg_id(),
                                                    product.getName(),
                                                    product.getDescription(),
                                                    product.getPrice(),
                                                    product.getImage(),
                                                    product.getIsActive(),
                                                    product.getQuantity()
                                            ),
                                            Collectors.toList()
                                    )
                            )
                    ));

            // Convertir el mapa a una lista de GroupedProductsResponseDto
            return groupedProducts.entrySet().stream()
                    .flatMap(categoryEntry -> categoryEntry.getValue().entrySet().stream()
                            .map(restaurantEntry -> new GroupedProductsResponseDto(
                                    categoryEntry.getKey(), // Nombre de la categoría
                                    products.stream() // Obtener el ID de la categoría
                                            .filter(p -> p.getCategory().getName().equals(categoryEntry.getKey()))
                                            .findFirst()
                                            .map(p -> p.getCategory().getCtg_id())
                                            .orElse(null),
                                    products.stream() // Obtener el nombre del restaurante
                                            .filter(p -> p.getRestaurant().getId().equals(restaurantEntry.getKey()))
                                            .findFirst()
                                            .map(p -> p.getRestaurant().getName())
                                            .orElse(null),
                                    restaurantEntry.getKey(), // ID del restaurante
                                    restaurantEntry.getValue() // Lista de productos
                            ))
                    )
                    .collect(Collectors.toList());
        }
}