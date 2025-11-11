package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.request.ProductRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.ProductUpdateDto;
import com.c24_39_t_webapp.restaurants.dtos.response.GroupedProductsResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductSummaryResponseDto;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.ProductNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.RestaurantNotFoundException;
import com.c24_39_t_webapp.restaurants.exception.UnauthorizedAccessException;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Product;
import com.c24_39_t_webapp.restaurants.models.Restaurant;

import com.c24_39_t_webapp.restaurants.repository.CategoryRepository;
import com.c24_39_t_webapp.restaurants.repository.ProductRepository;
import com.c24_39_t_webapp.restaurants.repository.RestaurantRepository;
import com.c24_39_t_webapp.restaurants.services.IProductService;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    @Override
    @Transactional
    public ProductResponseDto addProduct(ProductRequestDto productRequestDto) {
        log.info("Intentando crear un producto para el restaurante con ID: {}", productRequestDto.restaurantId());
//        if (restaurantId == null || restaurantId <= 0) {
//            throw new IllegalArgumentException("El ID del restaurante no es válido");
//        }
        Restaurant restaurant = restaurantRepository.findById(productRequestDto.restaurantId())
                .orElseThrow(() -> {
                    log.warn("Restaurante no encontrado con ID: {}", productRequestDto.restaurantId());
                    return new RestaurantNotFoundException("No se ha encontrado el restaurante con ID: " + productRequestDto.restaurantId());
                });
        Category category = categoryRepository.findById(productRequestDto.categoryId())
                .orElseThrow(() -> {
                    log.warn("Categoría no encontrada con ID: {}", productRequestDto.categoryId());
                    return new CategoryNotFoundException("No se ha encontrado la categoria con ID: " + productRequestDto.restaurantId());
                });
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Recuperando el email del usuario de la autenticacion: {}", userEmail);
        log.info("Recuperando el email del usuario del restaurante: {}", restaurant.getUserEntity().getEmail());
        log.info("Usuario autenticado con email: {}", userEmail);
        if (!restaurant.getUserEntity().getEmail().equals(userEmail)) {
            throw new UnauthorizedAccessException("No tienes permiso para añadir productos a este restaurante");
        }

        Product newProduct = new Product();
        newProduct.setName(productRequestDto.name());
        newProduct.setDescription(productRequestDto.description());
        newProduct.setPrice(productRequestDto.price());
        newProduct.setImage(productRequestDto.image());
        newProduct.setIsActive(productRequestDto.isActive() != null ? productRequestDto.isActive() : true); // Default true si no viene
        newProduct.setQuantity(productRequestDto.quantity());

        // --- 4. Asigna las ENTIDADES encontradas ---
        newProduct.setRestaurant(restaurant);
        newProduct.setCategory(category);
        Product savedProduct = productRepository.save(newProduct);
        log.info("Producto creado con éxito con ID: {}", savedProduct.getPrd_id());

        return new ProductResponseDto(
                savedProduct.getPrd_id(),
                savedProduct.getRestaurant().getId(),
                savedProduct.getCategory().getId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                savedProduct.getImage(),
                savedProduct.getIsActive(),
                savedProduct.getQuantity(),
                savedProduct.getCategory().getName(),
                savedProduct.getRestaurant().getName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findAllProducts() {
        log.info("Recuperando todos los productos.");
        List<Product> products = productRepository.findAll();

//        if (products.isEmpty()) {
//            throw new ProductNotFoundException("No se encontraron productos.");
//        }
        if (products.isEmpty())  return Collections.emptyList();

        return products.stream()
                .map(product -> new ProductResponseDto(
                        product.getPrd_id(),
                        product.getRestaurant().getId(),
                        product.getCategory().getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getImage(),
                        product.getIsActive(),
                        product.getQuantity(),
                        product.getCategory().getName(),
                        product.getRestaurant().getName(
                        ))
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
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
                        product.getCategory().getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getImage(),
                        product.getIsActive(),
                        product.getQuantity(),
                        product.getCategory().getName(),
                        product.getRestaurant().getName()
                ))
                .orElseThrow(() -> {
                    log.warn("No se encontro un producto con el ID: {}", prd_id);
                    return new ProductNotFoundException("No se encontro una producto con ese ID: " + prd_id);
                });
    }

    @Transactional
    @Override
    public ProductResponseDto updateProduct(Long productId, ProductUpdateDto updateDto) {
        log.info("Solicitud recibida para actualizar el producto con ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("El ID del producto proporcionado es invalido: {}", productId);
                    return new ProductNotFoundException("No se ha encontrado el producto con el ID " + productId);
                });
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (product.getRestaurant() == null || product.getRestaurant().getUserEntity() == null || !product.getRestaurant().getUserEntity().getEmail().equals(userEmail)) {
            log.warn("Permiso denegado: Usuario {} intentando actualizar producto {} del restaurante {}", userEmail, productId, product.getRestaurant() != null ? product.getRestaurant().getId() : "DESCONOCIDO");
            throw new UnauthorizedAccessException("No tienes permiso para actualizar este producto"); // O lanzar AccessDeniedException mapeada a 403
        }
        if (updateDto.name() != null) product.setName(updateDto.name());
        if (updateDto.description() != null) product.setDescription(updateDto.description());
        if (updateDto.price() != null) product.setPrice(updateDto.price());
        if (updateDto.image() != null) product.setImage(updateDto.image());
        if (updateDto.isActive() != null)
            product.setIsActive(updateDto.isActive()); // Asume tipo boolean/Boolean y que no viola NOT NULL
        if (updateDto.quantity() != null) product.setQuantity(updateDto.quantity()); // Asume tipo Integer/Long

        Category category = categoryRepository.findById(updateDto.categoryId())
                .orElseThrow(() -> {
                    log.warn("El ID de la categoria proporcionada es invalido: {}", updateDto.categoryId());
                    return new CategoryNotFoundException("No se ha encontrado la categoria con el ID " + updateDto.categoryId());
                });
        product.setCategory(category);

        Product updatedProductEntity = productRepository.save(product);
        log.info("Producto ID {} actualizado en BD", updatedProductEntity.getPrd_id());

        //    Necesita los nombres, los obtiene de las entidades cargadas/actualizadas
        String categoryName = (updatedProductEntity.getCategory() != null) ? updatedProductEntity.getCategory().getName() : null;
        String restaurantName = (updatedProductEntity.getRestaurant() != null) ? updatedProductEntity.getRestaurant().getName() : null;

        return new ProductResponseDto(
                updatedProductEntity.getPrd_id(),
                updatedProductEntity.getRestaurant() != null ? updatedProductEntity.getRestaurant().getId() : null,
                updatedProductEntity.getCategory() != null ? updatedProductEntity.getCategory().getId() : null,
                updatedProductEntity.getName(),
                updatedProductEntity.getDescription(),
                updatedProductEntity.getPrice(), // Asegura tipo
                updatedProductEntity.getImage(),
                updatedProductEntity.getIsActive(), // Asegura tipo
                updatedProductEntity.getQuantity(), // Asegura tipo
                categoryName,
                restaurantName
        );
    }

    @Override
    @Transactional
    public void deleteProduct(Long prd_id) {
        log.info("Solicitud recibida para eliminar el producto con ID: {}", prd_id);
        Product product = productRepository.findById(prd_id)
                .orElseThrow(() -> {
                    log.warn("Intento de eliminar producto no existente con ID: {}", prd_id);
                    return new ProductNotFoundException("Producto no encontrado con id: " + prd_id);
                });
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (product.getRestaurant().getUserEntity() == null || !product.getRestaurant().getUserEntity().getEmail().equals(userEmail)) {
            log.warn("Permiso denegado: Usuario {} intentando eliminar producto {} del restaurante {}",
                    userEmail, prd_id, product.getRestaurant() != null ? product.getRestaurant().getId() : "DESCONOCIDO");
            throw new UnauthorizedAccessException("No tienes permiso para eliminar este producto");
        }
        productRepository.deleteById(prd_id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryResponseDto> findProductsByCategoryId(Long categoryId) {
        log.info("Recuperando los producto de la categoria con ID {}", categoryId);
        List<Product> products = productRepository.findProductsByCategoryId(categoryId);

        if (products.isEmpty())  return Collections.emptyList();

        return products.stream()
                .map(product -> new ProductSummaryResponseDto(
                        product.getPrd_id(),
                        product.getRestaurant().getId(),
                        product.getCategory().getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getImage()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
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
                        product.getCategory().getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getImage()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findProductsByRestaurantId(Long restaurantId) {
        log.info("Buscando productos del restaurante con ID: {}", restaurantId);

        List<Product> products = productRepository.findProductsByRestaurantId(restaurantId);
        if (products.isEmpty())  return Collections.emptyList();

        return products.stream()
                .map(product -> new ProductResponseDto(
                        product.getPrd_id(),
                        product.getRestaurant().getId(),
                        product.getCategory().getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getImage(),
                        product.getIsActive(),
                        product.getQuantity(),
                        product.getCategory().getName(),
                        product.getRestaurant().getName(
                        ))
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupedProductsResponseDto> findProductsByRestaurantIdAndCategory(Long restaurantId) {
        log.info("Buscando productos del restaurante con ID: {}", restaurantId);

        List<ProductResponseDto> productDtos = productRepository.findProductsByRestaurantIdAndCategory(restaurantId);
        if(productDtos.isEmpty())  return Collections.emptyList();

        // Agrupa los DTOs en memoria por categoryId
        Map<Long, List<ProductResponseDto>> groupedByCategoryId = productDtos.stream()
                .collect(Collectors.groupingBy(
                        ProductResponseDto::categoryId, // Agrupa por ID de categoría
                        TreeMap::new,                      // Para ordenar por ID de categoría
                        Collectors.toList()                // Lista de productos (DTOs) por categoría
                ));

        // Construye la respuesta final agrupada.
        List<GroupedProductsResponseDto> finalResponse = groupedByCategoryId.entrySet().stream()
                .map(entry -> {
                    Long categoryId = entry.getKey();
                    List<ProductResponseDto> productsInCategory = entry.getValue();
                    // Hacer una consulta a CategoryRepository o tener un Map<Long, String>
//                    String categoryName = productsInCategory.isEmpty() ? "Unknown Category" : productsInCategory.get(0).categoryName();
                    String categoryName = productsInCategory.isEmpty() ? "Unknown Category" : productsInCategory.getFirst().categoryName();
                    Long restId = productsInCategory.isEmpty() ? null : productsInCategory.getFirst().restaurantId();
                    String restName = productsInCategory.isEmpty() ? "Unknown Restaurant" : productsInCategory.getFirst().restaurantName();

                    return new GroupedProductsResponseDto(categoryName, categoryId, restName, restId, productsInCategory);
                })
                .collect(Collectors.toList());
        log.info("Se agruparon productos de {} categorías para el restaurante {}", finalResponse.size(), restaurantId);
        return finalResponse;
    }
}