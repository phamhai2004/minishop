package com.example.minishop.controller.admin;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.CategoryRequest;
import com.example.minishop.dto.response.CategoryResponse;
import com.example.minishop.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllCategories() {
        return ResponseEntity.ok(
                ApiResponse.success(categoryService.getAll())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(categoryService.getById(id))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {
        CategoryResponse category = categoryService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo danh mục thành công", category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request
    ) {
        CategoryResponse category = categoryService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật danh mục thành công", category)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> searchCategories(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(categoryService.searchByName(keyword))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success("Xóa danh mục thành công", null)
        );
    }
}