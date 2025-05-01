package com.c24_39_t_webapp.restaurants.controllers;


import com.c24_39_t_webapp.restaurants.dtos.request.ProductRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.request.ProductUpdateDto;
import com.c24_39_t_webapp.restaurants.dtos.response.GroupedProductsResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductResponseDto;
import com.c24_39_t_webapp.restaurants.dtos.response.ProductSummaryResponseDto;
import com.c24_39_t_webapp.restaurants.repository.CategoryRepository;
import com.c24_39_t_webapp.restaurants.repository.ProductRepository;
import com.c24_39_t_webapp.restaurants.services.IProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/product")
public class ProductController {

    @Autowired
    private final IProductService productService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public ProductController(IProductService productService, CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

//    public ProductController(IProductService productService) {
//        this.productService = productService;
//    }
    /**
     * Endpoint to add a new {@link ResponseEntity} object to the system.
     * Delegates the addition logic to {@link IProductService#addProduct(ProductRequestDto)}.
     *
     * @param requestDto The {@code CategoryRequestDto} object to add.
     * @return The {@code CategoryResponseDto} object representing the added category.
     */
    @PostMapping
    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<ProductResponseDto> addProduct(@Valid @RequestBody ProductRequestDto requestDto) {
        log.info("Recibida solicitud para añadir un producto al restaurante con ID: {}", requestDto.restaurantId());
        log.info("Datos del producto: {}", requestDto);

        ProductResponseDto responseDto = productService.addProduct(requestDto);
        log.info("Producto agregado exitosamente: {}", responseDto);
//        return ResponseEntity.ok(responseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Endpoint to retrieve all {@link ProductResponseDto} objects from the system.
     * Delegates the retrieval logic to {@link IProductService#findAllProducts()}.
     *
     * @return A list of {@code ProductResponseDto} objects representing all products in the system.
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        log.info("Solicitud recibida para obtener todos los productos.");
        List<ProductResponseDto> products = productService.findAllProducts();
        log.info("Se recuperaron {} productos exitosamente.", products.size());
        return ResponseEntity.ok(products);
    }

    /**
     * Endpoint to retrieve a single {@link ProductResponseDto} object from the system.
     * Delegates the retrieval logic to {@link IProductService#findProductById(Long)}.
     *
     * @param prd_id The ID of the product to retrieve.
     * @return The {@code ProductResponseDto} object representing the requested product.
     */
    @GetMapping("/{prd_id}")
    public ResponseEntity<ProductResponseDto> findProductById(@PathVariable Long prd_id) {
        log.info("Solicitud recibida para obtener una producto usando el ID {}.", prd_id);
        ProductResponseDto product = productService.findProductById(prd_id);
        log.info("Se recuperó el producto con ID {} exitosamente.", prd_id);
        return ResponseEntity.ok(product);
    }

    /**
     * Endpoint to update an existing {@link ProductResponseDto} object in the system.
     * Delegates the update logic to {@link IProductService#updateProduct(Long, ProductUpdateDto)}.
     *
     * @param prd_id The ID of the product to update.
     * @param updateDto The {@code ProductRequestDto} object containing the updated product details.
     * @return The {@code ProductResponseDto} object representing the updated product.
     */
    @PatchMapping("/{prd_id}")
    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long prd_id,
            @RequestBody @Valid ProductUpdateDto updateDto
    ) {
        log.info("Solicitud recibida para actualizar el producto con ID: {}", prd_id);
        ProductResponseDto updatedDto = productService.updateProduct(prd_id, updateDto);
        log.info("Controller: Producto actualizado, devolviendo DTO: {}", updatedDto);
        return ResponseEntity.ok(updatedDto);
    }

    /**
     * Endpoint to delete an existing {@link ProductResponseDto} object from the system.
     * Delegates the deletion logic to {@link IProductService#deleteProduct(Long)}.
     *
     * @param prd_id The ID of the product to delete.
     * @return A {@link ResponseEntity} object with no content to indicate a successful deletion.
     */
    @DeleteMapping("/{prd_id}")
    @PreAuthorize("hasRole('RESTAURANTE')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long prd_id) {
        log.info("Solicitud recibida para eliminar el producto con ID: {}", prd_id);
        productService.deleteProduct(prd_id);
        log.info("Producto con ID: {} eliminado exitosamente", prd_id);
        return ResponseEntity.noContent().build();
    }
    /**
        * Endpoint to retrieve a list of all {@link ProductSummaryResponseDto} objects stored in the system.
        * Delegates the retrieval logic to {@link IProductService#findProductsByCategoryId(Long)}.
        *
        * @param categoryId The category to retrieve products for.
        * @return A list of {@code ProductSummaryResponseDto} objects representing all products in the specified category.
        */
    @GetMapping(value = "/byCategory/{category}")
    public ResponseEntity<List<ProductSummaryResponseDto>> findProductsByCategoryId(@PathVariable Long categoryId) {
        log.info("Solicitud recibida para obtener productos por categoria con ID: {}", categoryId);
        List<ProductSummaryResponseDto> products = productService.findProductsByCategoryId(categoryId);
        log.info("Se recuperaron {} productos por categoria con ID: {} exitosamente.", products.size(), categoryId);
        return ResponseEntity.ok(products);
    }
    /**
     * Endpoint to retrieve a list of all {@link ProductSummaryResponseDto} objects stored in the system.
     * Delegates the retrieval logic to {@link IProductService#findProductsByName(String)}.
     *
     * @param name The name of the product to retrieve.
     * @return A list of {@code ProductSummaryResponseDto} objects representing all products with the specified name.
     */
    @GetMapping(value = "/byName")
    public ResponseEntity<List<ProductSummaryResponseDto>> findProductsByName(@RequestParam String name) {
        log.info("Solicitud recibida para obtener productos por categoria con ID: {}", name);
        List<ProductSummaryResponseDto> products = productService.findProductsByName(name);
        log.info("Se recuperaron {} productos por categoria con ID: {} exitosamente.", products.size(), name);
        return ResponseEntity.ok(products);
    }

    /**
     * Endpoint to retrieve a list of all {@link ProductSummaryResponseDto} objects stored in the system.
     * Delegates the retrieval logic to {@link IProductService#findProductsByRestaurantId(Long)}.
     *
     * @param restaurantId The id of the restaurant to retrieve products for.
     * @return A list of {@code ProductSummaryResponseDto} objects representing all products in the specified restaurant.
     */
    @GetMapping(value = "/byRestaurant/{restaurantId}")
    public ResponseEntity<List<ProductResponseDto>> findProductsByRestaurantId(@PathVariable Long restaurantId) {
        log.info("Solicitud recibida para obtener productos del restaurante con ID: {}", restaurantId);
        List<ProductResponseDto> products = productService.findProductsByRestaurantId(restaurantId);
        log.info("Se recuperaron {} productos del restaurante: {} exitosamente.", products.size(), restaurantId);
        return ResponseEntity.ok(products);
    }

    /**
     * Endpoint to retrieve a list of all {@link GroupedProductsResponseDto} objects stored in the system.
     * Delegates the retrieval logic to {@link IProductService#findProductsByRestaurantIdAndCategory(Long)}.
     *
     * @param restaurantId The restaurant to retrieve products for.
     * @return A list of {@code GroupedProductsResponseDto} objects representing
     * all products in the specified restaurant grouped by categories.
     */
    @GetMapping(value = "/byRestaurantAndCategory/{restaurantId}")
    public ResponseEntity<List<GroupedProductsResponseDto>> findProductsByRestaurantAndCategory(@PathVariable Long restaurantId) {
        log.info("Solicitud recibida para obtener productos del restaurante: {}", restaurantId);
        List<GroupedProductsResponseDto> products = productService.findProductsByRestaurantIdAndCategory(restaurantId);
        log.info("Se recuperaron productos de {} categorias del restaurante: {} exitosamente.", products.size(), restaurantId);
        return ResponseEntity.ok(products);
    }
}
