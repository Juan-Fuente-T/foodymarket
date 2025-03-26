package com.c24_39_t_webapp.restaurants.services.impl;

import com.c24_39_t_webapp.restaurants.dtos.request.CategoryRequestDto;
import com.c24_39_t_webapp.restaurants.dtos.response.CategoryResponseDto;
import com.c24_39_t_webapp.restaurants.exception.CategoryNotFoundException;
import com.c24_39_t_webapp.restaurants.models.Category;
import com.c24_39_t_webapp.restaurants.models.Restaurant;
import com.c24_39_t_webapp.restaurants.repository.CategoryRepository;
import com.c24_39_t_webapp.restaurants.repository.ProductRepository;
import com.c24_39_t_webapp.restaurants.services.ICategoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public CategoryResponseDto addCategory(Category category) {
        log.info("Agregando una nueva categoria: {}", category.getName());

        // Verificar si ya existe una categoría con el mismo nombre
        Category existingCategory = categoryRepository.findByName(category.getName());
        if (existingCategory != null) {
            log.warn("Intento de crear una categoría con un nombre duplicado. Nombre: {}", category.getName());
            throw new IllegalArgumentException("Ya existe una categoría con este nombre.");
        }

        Category savedCategory  = categoryRepository.save(category);

        CategoryResponseDto categoryResponseDto = new CategoryResponseDto(
                savedCategory.getCtg_id(),
                savedCategory.getName(),
                savedCategory.getDescription()
        );
        log.info("Categoria agregada exitosamente: {}", categoryResponseDto);
        return categoryResponseDto;
    }
    @Override
    public List<CategoryResponseDto> findAllCategories() {


        List<Category> categories = categoryRepository.findAll();

        if (categories.isEmpty()) {
            throw new CategoryNotFoundException("No se encontraron categorias.");
        }

        return categories.stream()
                .map(category -> new CategoryResponseDto(
                        category.getCtg_id(),
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
                        category.getCtg_id(),
                        category.getName(),
                        category.getDescription()
                ))
                .orElseThrow(() -> {
                    log.warn("No se encontro una categoria con el ID: {}", ctg_id);
                    return new CategoryNotFoundException("No se encontro una categoria con ese ID: " + ctg_id);
                });
    }

    @Override
    public CategoryResponseDto updateCategory(Category category) {
        log.info("Actualizando la categoria con ID {}", category.getCtg_id());

        Category updatedCategory = categoryRepository.save(category);

        log.info("Categoria actualizado exitosamente: {}", updatedCategory);
        return new CategoryResponseDto(
                updatedCategory.getCtg_id(),
                updatedCategory.getName(),
                updatedCategory.getDescription()
        );
    }

    @Override
    public void deleteCategory(Long ctg_id) {
        if (!categoryRepository.existsById(ctg_id)) {
            throw new CategoryNotFoundException("Restaurante no encontrado con id: " + ctg_id);
        }
        categoryRepository.deleteById(ctg_id);
    }
}