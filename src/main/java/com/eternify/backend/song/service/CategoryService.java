package com.eternify.backend.song.service;

import com.eternify.backend.song.model.Category;

import java.util.List;

public interface CategoryService {
    void addCategory(String name);
    void deleteCategory(String id);
    Category getCategoryById(String id);
    Category getCategoryByName(String name);
    void addFavouriteCategory(List<String> categoryIds);
    List<Category> getAllCategories();
}
