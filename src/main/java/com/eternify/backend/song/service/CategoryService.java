package com.eternify.backend.song.service;

import com.eternify.backend.song.model.Category;

public interface CategoryService {
    void addCategory(String name);
    void deleteCategory(String id);
    Category getCategoryById(String id);
    Category getCategoryByName(String name);
}
