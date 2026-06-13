package com.myproject.inventorymanagement.service;

import com.myproject.inventorymanagement.entity.Category;
import com.myproject.inventorymanagement.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public List<Category> searchCategories(String name){
        return categoryRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public Category createCategory(Category category){
        if(categoryRepository.existsByNameIgnoreCase(category.getName())){
            throw new RuntimeException("Category name already exists: " + category.getName());
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category updated){
        Category existing = getCategoryById(id);
        // Category name is fixed and cannot be changed
        existing.setDescription(updated.getDescription());
        return categoryRepository.save(existing);
    }

    @Transactional
    public void deleteCategory(Long id){
        if(!categoryRepository.existsById(id)){
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
