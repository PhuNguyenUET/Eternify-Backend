package com.eternify.backend.song.service.implementation;

import com.eternify.backend.common.exception.BusinessException;
import com.eternify.backend.song.model.Category;
import com.eternify.backend.song.repository.CategoryRepository;
import com.eternify.backend.song.service.CategoryService;
import com.eternify.backend.user.model.User;
import com.eternify.backend.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public void addCategory(String name) {
        Category categoryCheck = categoryRepository.findByName(name);

        if(categoryCheck != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Category already exists");
        }

        Category category = Category.builder()
                .name(name)
                .build();

        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id).orElse(null);

        if(category == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Category doesn't exist");
        }

        categoryRepository.delete(category);
    }

    @Override
    public Category getCategoryById(String id) {
        Category category = categoryRepository.findById(id).orElse(null);

        if(category == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Category doesn't exist");
        }

        return category;
    }

    @Override
    public Category getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name);

        if(category == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Category doesn't exist");
        }

        return category;
    }

    @Override
    public void addFavouriteCategory(List<String> categoryIds) {
        User currentUser = AuthenticationUtils.getCurrentUser();

        for(String categoryId : categoryIds) {
            currentUser.getUserPref().getCategoryFrequency().put(categoryId, currentUser.getUserPref().getCategoryFrequency().getOrDefault(categoryId, 0) + 10);
        }
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
