package com.hack.InventoryManagementSystem.services;

import com.hack.InventoryManagementSystem.dto.CategoryDTO;
import com.hack.InventoryManagementSystem.dto.Response;

public interface CategoryService{
    Response createCategory(CategoryDTO categoryDTO);
    Response getAllCategories();
    Response getCategoryById(Long id);
    Response updateCategory(Long id, CategoryDTO categoryDTO);
    Response deleteCategory(Long id);
}
