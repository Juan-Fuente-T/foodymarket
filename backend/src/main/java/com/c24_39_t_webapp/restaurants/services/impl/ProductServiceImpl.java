package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.request.ProductRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.ProductUpdateDto;
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
    public ProductResponseDto addProduct(ProductRequestDto productRequestDto){
        log.info("Intentando crear un producto para el restaurante con ID: {}", productRequestDto.restaurantId());
//        if (restaurantId == null || restaurantId <= 0) {
//            throw new IllegalArgumentException("El ID del restaurante no es válido");
//        }
        Restaurant restaurant = restaurantRepository.findById(productRequestDto.restaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException("No se ha encontrado el restaurante"));
        log.warn("Restaurante no encontrado con ID: {}", productRequestDto.restaurantId());
        Category category = categoryRepository.findById(productRequestDto.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("No se ha encontrado la categoria"));
        log.warn("Categoría no encontrada con ID: {}", productRequestDto.categoryId());
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Recuperando el email del usuario de la autenticacion: {}", userEmail);
        log.info("Recuperando el email del usuario del restaurante: {}", restaurant.getUserEntity().getEmail());
        log.info("Usuario autenticado con email: {}", userEmail);
        if (!restaurant.getUserEntity().getEmail().equals(userEmail)) {
            throw new SecurityException("No tienes permiso para añadir productos a este restaurante");
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
            throw new SecurityException("No tienes permiso para actualizar este producto"); // O lanzar AccessDeniedException mapeada a 403
        }
        if (updateDto.name() != null) product.setName(updateDto.name());
        if (updateDto.description() != null) product.setDescription(updateDto.description());
        if (updateDto.price() != null) product.setPrice(updateDto.price());
        if (updateDto.image() != null) product.setImage(updateDto.image());
        if (updateDto.isActive() != null) product.setIsActive(updateDto.isActive()); // Asume tipo boolean/Boolean y que no viola NOT NULL
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
                        product.getCategory().getId(),
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
                        product.getCategory().getId(),
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
    public List<GroupedProductsResponseDto> findProductsByRestaurantIdAndCategory(Long restaurantId) {
       log.info("Buscando productos del restaurante con ID: {}", restaurantId);
//
//            // Obtener los productos del repositorio
//            List<Product> products = productRepository.findProductsByRestaurantIdAndCategory(restaurantId);
//
//            // Agrupar los productos por categoría y restaurante
//            Map<String, Map<Long, List<ProductResponseDto>>> groupedProducts = products.stream()
//                    .collect(Collectors.groupingBy(
//                            product -> product.getCategory().getName(), // Agrupar por nombre de categoría
//                            TreeMap::new, // Ordenar por nombre de categoría
//                            Collectors.groupingBy(
//                                    product -> product.getRestaurant().getId(), // Agrupar por ID del restaurante
//                                    TreeMap::new, // Ordenar por ID del restaurante
//                                    Collectors.mapping(
//                                            product -> new ProductResponseDto(
//                                                    product.getPrd_id(),
//                                                    product.getRestaurant().getId(),
//                                                    product.getCategory().getId(),
//                                                    product.getName(),
//                                                    product.getDescription(),
//                                                    product.getPrice(),
//                                                    product.getImage(),
//                                                    product.getIsActive(),
//                                                    product.getQuantity()
//                                            ),
//                                            Collectors.toList()
//                                    )
//                            )
//                    ));
//
//            // Convertir el mapa a una lista de GroupedProductsResponseDto
//            return groupedProducts.entrySet().stream()
//                    .flatMap(categoryEntry -> categoryEntry.getValue().entrySet().stream()
//                            .map(restaurantEntry -> new GroupedProductsResponseDto(
//                                    categoryEntry.getKey(), // Nombre de la categoría
//                                    products.stream() // Obtener el ID de la categoría
//                                            .filter(p -> p.getCategory().getName().equals(categoryEntry.getKey()))
//                                            .findFirst()
//                                            .map(p -> p.getCategory().getId())
//                                            .orElse(null),
//                                    products.stream() // Obtener el nombre del restaurante
//                                            .filter(p -> p.getRestaurant().getId().equals(restaurantEntry.getKey()))
//                                            .findFirst()
//                                            .map(p -> p.getRestaurant().getName())
//                                            .orElse(null),
//                                    restaurantEntry.getKey(), // ID del restaurante
//                                    restaurantEntry.getValue() // Lista de productos
//                            ))
//                    )
//                    .collect(Collectors.toList());

        List<ProductResponseDto> productDtos = productRepository.findProductsByRestaurantIdAndCategory(restaurantId);

        // Agrupa los DTOs en memoria por categoryId (si GroupedProductsResponseDto lo necesita)
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
                    String categoryName = productsInCategory.isEmpty() ? "Unknown Category" : productsInCategory.get(0).categoryName();
                    Long restId = productsInCategory.isEmpty() ? null : productsInCategory.get(0).restaurantId();
                    String restName = productsInCategory.isEmpty() ? "Unknown Restaurant" : productsInCategory.get(0).restaurantName();

                    return new GroupedProductsResponseDto(categoryName, categoryId, restName, restId, productsInCategory);
                })
                .collect(Collectors.toList());
        log.info("Se agruparon productos de {} categorías para el restaurante {}", finalResponse.size(), restaurantId);
        return finalResponse;
        }
}