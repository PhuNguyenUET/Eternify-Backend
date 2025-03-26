package com.eternify.backend.song.controller;

import com.eternify.backend.common.api.ApiResponse;
import com.eternify.backend.song.dto.SongDTO;
import com.eternify.backend.song.model.Category;
import com.eternify.backend.song.service.CategoryService;
import com.eternify.backend.song.service.TagService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "category")
@RequestMapping("/api/category/v1")
public class CategoryController {
    @Value("${api.token}")
    private String apiToken;

    private final CategoryService categoryService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addCategory(@RequestHeader("X-auth-token") String token,
                                              @RequestBody String name) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            categoryService.addCategory(name);
            return ResponseEntity.ok(ApiResponse.success("Add category success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteCategory(@RequestHeader("X-auth-token") String token,
                                                 @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Delete category success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = Category.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getCategoryById(@RequestHeader("X-auth-token") String token,
                                                 @RequestParam String id) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get category by id success", categoryService.getCategoryById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/get_by_name")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = Category.class), mediaType = "application/json") }),
    })
    public ResponseEntity<ApiResponse> getCategoryByName(@RequestHeader("X-auth-token") String token,
                                                 @RequestParam String name) {
        try {
            Assert.isTrue(apiToken.equals(token), "Invalid token");
            return ResponseEntity.ok(ApiResponse.success("Get category by name success", categoryService.getCategoryByName(name)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/add_favourite")
    public ResponseEntity<ApiResponse> addFavouriteCategories(@RequestBody List<String> categoryIds) {
        try {
            categoryService.addFavouriteCategory(categoryIds);
            return ResponseEntity.ok(ApiResponse.success("Add favourite categories success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()));
        }
    }
}
