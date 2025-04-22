package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.request.CategoryRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.repository.CategoryRepository;
import com.c24_39_t_webapp.restaurants.repository.ProductRepository;
import com.c24_39_t_webapp.restaurants.repository.RestaurantRepository;
import com.c24_39_t_webapp.restaurants.services.ICategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final RestaurantRepository restaurantRepository;

//    @Override
//    public CategoryResponseDto addCategory(Category category) {
//        log.info("Agregando una nueva categoria: {}", category.getName());
//
//        // Verificar si ya existe una categoría con el mismo nombre
//        Optional<Category> existingCategory = categoryRepository.findByName(category.getName());
//        if (existingCategory != null) {
//            log.warn("Intento de crear una categoría con un nombre duplicado. Nombre: {}", category.getName());
//            throw new IllegalArgumentException("Ya existe una categoría con este nombre.");
//        }
//
//        Category savedCategory  = categoryRepository.save(category);
//
//
//
//        CategoryResponseDto categoryResponseDto = new CategoryResponseDto(
//                savedCategory.getId(),
//                savedCategory.getName(),
//                savedCategory.getDescription()
//        );
//        log.info("Categoria agregada exitosamente: {}", categoryResponseDto);
//        return categoryResponseDto;
//    }
    @Override
    // @Transactional // Ya está a nivel de clase, pero ponerlo aquí no hace daño
    public Category findOrCreateCategory(CategoryRequestDto categoryInput) {
        log.info("Buscando o creando categoría global con nombre: {}", categoryInput.name());
        // Usamos orElseGet para buscar y si no, ejecutar la lambda para crear
        return categoryRepository.findByName(categoryInput.name())
                .orElseGet(() -> {
                    log.info("Categoría '{}' no encontrada globalmente, creando nueva...", categoryInput.name());
                    Category newCategory = new Category();
                    newCategory.setName(categoryInput.name());
                    newCategory.setDescription(categoryInput.description());
                    Category savedCategory = categoryRepository.save(newCategory);
                    log.info("Nueva categoría global creada con ID: {}", savedCategory.getId());
                    return savedCategory;
                });
    }

    @Override
    public List<CategoryResponseDto> findAllCategories() {


        List<Category> categories = categoryRepository.findAll();

        if (categories.isEmpty()) {
            throw new CategoryNotFoundException("No se encontraron categorias.");
        }

        return categories.stream()
                .map(category -> new CategoryResponseDto(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDto findCategoryById(Long ctg_id) {
        log.info("Buscando categoria con ID: {}", ctg_id);
//        Restaurant restaurant = restaurantRepository.findById(id)
        if (ctg_id == null || ctg_id <= 0) {
            log.warn("El ID de la categoria proporcionada es invalido: {}", ctg_id);
            throw new CategoryNotFoundException("El ID de la categoria no es válido " + ctg_id);
        }
        return categoryRepository.findById(ctg_id)
                .map(category -> new CategoryResponseDto(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ))
                .orElseThrow(() -> {
                    log.warn("No se encontro una categoria con el ID: {}", ctg_id);
                    return new CategoryNotFoundException("No se encontro una categoria con ese ID: " + ctg_id);
                });
    }
//    @Override
//    public CategoryResponseDto updateCategory(Category category) {
//        log.info("Actualizando la categoria con ID {}", category.getId());
//
//        Category updatedCategory = categoryRepository.save(category);
//
//        log.info("Categoria actualizado exitosamente: {}", updatedCategory);
//        return new CategoryResponseDto(
//                updatedCategory.getId(),
//                updatedCategory.getName(),
//                updatedCategory.getDescription()
//        );
//    }

    @Override
    public void deleteCategory(Long restaurantId, Long categoryId) {
        log.info("Intentando eliminar categoría global con ID: {}", categoryId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante no encontrado con ID: " + restaurantId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada con ID: " + categoryId));

        // --- Desasociar del restaurante actual ---
        boolean removed = restaurant.getOfferedCategories().remove(category);
        if (removed) {
            restaurantRepository.save(restaurant); // Guarda para efectuar la eliminación para ese restaurante
            log.info("Categoría ID {} desasociada del restaurante ID {}", categoryId, restaurantId);

            // --- Comprobar si no hay restaurantes o productos con esa categoria ---
            boolean isUsedByProduct = productRepository.existsByCategoryId(categoryId);
            long remainingRestaurantCount = categoryRepository.countRestaurantsUsingCategory(categoryId);

            log.debug("Categoría ID {}: Usada por productos: {}, Usada por otros restaurantes: {}",
                    categoryId,
                    isUsedByProduct,
                    remainingRestaurantCount
            );

            if (!isUsedByProduct && remainingRestaurantCount == 0) {
                log.info("La categoría ID {} ya es usada por otro producto o restaurante. Se borra globalmente.",
                        categoryId
                );
                try {
                    categoryRepository.deleteById(categoryId); // Intenta borrarla globalmente
                    log.info("Categoría global ID {} eliminada con éxito.", categoryId);
                } catch (Exception e) {
                    log.error("Error inesperado al intentar borrar globalmente la categoría ID {}: {}",
                            categoryId,
                            e.getMessage(), e
                    );
                    // Decidir si relanzar la excepción o no, dependiendo de la política de errores
                }
            } else {
                log.info("La categoría ID {} se ha eliminado para este restaurante, pero sigue en uso por productos o " +
                        "por otros restaurantes. No se borrará globalmente.", categoryId);
            }

        } else {
            log.warn("La categoría ID {} no estaba asociada al restaurante ID {} al intentar removerla.",
                    categoryId,
                    restaurantId
            );
            // Opcionalmente, aún podría comprobarse si debe borrarse globalmente si no estaba asociada
            // boolean isUsedByProduct = productRepository.existsByCategoryId(categoryId);
            // long remainingRestaurantCount = categoryRepository.countRestaurantsUsingCategory(categoryId);
            // if (!isUsedByProduct && remainingRestaurantCount == 0) { ... }
        }
    }
    }