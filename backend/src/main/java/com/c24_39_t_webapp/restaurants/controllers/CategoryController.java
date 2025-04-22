package com.c24_39_t_webapp.restaurants.controllers;

import com.c24_39_t_webapp.restaurants.dtos.request.CategoryRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.repository.CategoryRepository;
import com.c24_39_t_webapp.restaurants.services.ICategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/category")
public class CategoryController {
    @Autowired
    private ICategoryService categoryService;
    private CategoryRepository categoryRepository;

    /**
     * Test endpoint to verify that the controller handles POST requests correctly.
     * Responds with a message including the received name parameter.
     *
     * @param name The name parameter received in the request body.
     * @return A string indicating that the controller works and including the received name.
     */
    @PostMapping(value = "/testPostMethod")
    public String testControllerPost(@RequestBody String name) {
        return "El metodo POST del controller de Categories funciona ok, " + name + "!";
    }

    /**
     * Another test endpoint demonstrating GET request handling.
     * Responds with a custom message.
     *
     * @return A string indicating that the GET method works.
     */
    @GetMapping(value = "/testMethod")
    public String testControllerGet() {
        return "El metodo GET del controller de Categories funciona ok!";
    }

    /**
     * Endpoint to add a new {@link ResponseEntity} object to the system.
     * Delegates the addition logic to {@link ICategoryService#addCategory(Category)}.
     *
     * @param categoryRequestDto The {@code CategoryRequestDto} object to add.
     * @return The {@code CategoryResponseDto} object representing the added category.
     */
//    @PostMapping
//    public ResponseEntity<CategoryResponseDto> addCategory(@RequestBody CategoryRequestDto categoryRequestDto) {
//        log.info("Solicitud recibida para agregar una nueva categoria: {}", categoryRequestDto);
//        Category category = new Category();
//        category.setName(categoryRequestDto.name());
//        category.setDescription(categoryRequestDto.description());
//        CategoryResponseDto categoryResponseDto = categoryService.addCategory(category);
//        log.info("Categoria agregada exitosamente: {}", categoryResponseDto);
//        return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponseDto);
//    }


    /**
     * Endpoint to retrieve a list of all {@link ResponseEntity} objects stored in the system.
     * Delegates the retrieval logic to {@link ICategoryService#findAllCategories()}.
     *
     * @return A list of {@code ContactDTO} objects representing all contacts.
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        log.info("Solicitud recibida para obtener todos las categorias.");
        List<CategoryResponseDto> categories = categoryService.findAllCategories();
        log.info("Se recuperaron {} categorias exitosamente.", categories.size());
        return ResponseEntity.ok(categories);
    }

    /**
     * Endpoint to query a single category based on the provided {@link CategoryResponseDto}.
     * Delegates the query logic to {@link ICategoryService#findCategoryById(Long)}.
     *
     * @param ctg_id The {@link CategoryResponseDto} object containing the criteria for the query.
     * @return The {@code CategoryResponseDto} object representing the queried category.
     */
    @GetMapping("/{ctg_id}")
    public ResponseEntity<CategoryResponseDto> findCategoryById(@PathVariable Long ctg_id) {
        log.info("Solicitud recibida para obtener una producto usando el ID {}.", ctg_id);
        CategoryResponseDto category = categoryService.findCategoryById(ctg_id);
        log.info("Se recuperó la categoría con ID {} exitosamente.", ctg_id);
        return ResponseEntity.ok(category);
    }

    /**
     * Endpoint to update an existing category in the system using the provided {@link CategoryRequestDto}.
     * Delegates the update logic to {@link ICategoryService#updateCategory(Category)}.
     *
     * @param ctg_id    The ID of the category to update.
     * @param updateDto The {@link CategoryRequestDto} object containing the details of the updated category.
     * @return The {@code CategoryResponseDto} object representing the updated category.
     */
//    @PatchMapping("/{ctg_id}")
//    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable Long ctg_id, @RequestBody CategoryRequestDto updateDto) {
//        log.info("Solicitud recibida para actualizar la categoria con ID: {}", ctg_id);
//
//        Category category = categoryRepository.findById(ctg_id)
//                .orElseThrow(() -> {
//                    log.warn("No se encontró una categoria con ese ID para editar: {}", ctg_id);
//                    return new CategoryNotFoundException(("No se encontró una categoria con ese ID para editar: " + ctg_id));
//                });
//        category.setName(updateDto.name());
//        category.setDescription(updateDto.description());
//
//        CategoryResponseDto updatedCategory = categoryService.updateCategory(category);
//        log.info("Categoria con ID: {} actualizado exitosamente", ctg_id);
//        return ResponseEntity.ok(updatedCategory);
//    }

    /**
     * Endpoint to delete a category from the system using the provided {@link CategoryRequestDto}.
     * Delegates the deletion logic to {@link ICategoryService#deleteCategory(Long, Long)}.
     *  @param restaurantId The ID of the restaurant to which the category belongs.
     *  @param categoryId   The ID of the category to delete.
     *  @return A {@link ResponseEntity} indicating the result of the deletion operation.
     */
     @DeleteMapping("/{categoryId}/restaurant/{restaurantId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId, @PathVariable Long restaurantId) {
        log.info("Solicitud recibida para eliminar la categoria con ID: {} del restaurante con ID: {}.", categoryId, restaurantId);
        categoryService.deleteCategory(restaurantId, categoryId);
        log.info("Categoria con ID: {} del restaurante con ID {} se ha eliminado exitosamente", categoryId, restaurantId);
        return ResponseEntity.noContent().build();
    }

}