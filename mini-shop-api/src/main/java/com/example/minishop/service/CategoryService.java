package com.example.minishop.service;

import com.example.minishop.dto.request.CategoryRequest;
import com.example.minishop.dto.response.CategoryResponse;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.CategoryMapper;
import com.example.minishop.entity.Category;
import com.example.minishop.repository.CategoryRepository;
import com.example.minishop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(
            CategoryMapper categoryMapper,
            CategoryRepository categoryRepository,
            ProductRepository productRepository
    ) {
        this.categoryMapper = categoryMapper;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse getById(Long id) {
        Category category = getEntityById(id);
        return categoryMapper.toResponse(category);
    }

    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Tên danh mục đã tồn tại");
        }

        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getEntityById(id);

        categoryMapper.updateEntity(category, request);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    public void delete(Long id) {
        Category category = getEntityById(id);

        if (productRepository.existsByCategory_Id(id)) {
            throw new BadRequestException("Không thể xóa danh mục đang có sản phẩm");
        }

        categoryRepository.delete(category);
    }

    public List<CategoryResponse> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestException("Keyword không được để trống");
        }

        return categoryRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public Category getEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy danh mục với id: " + id
                ));
    }
}