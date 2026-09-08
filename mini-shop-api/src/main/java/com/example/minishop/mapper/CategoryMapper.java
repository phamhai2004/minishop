package com.example.minishop.mapper;


import com.example.minishop.dto.request.CategoryRequest;
import com.example.minishop.dto.response.CategoryResponse;
import com.example.minishop.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public Category toEntity (CategoryRequest categoryRequest){
        Category ca = new Category();
        ca.setName(categoryRequest.getName());
        ca.setDescription(categoryRequest.getDescription());
        return ca;
    }
    public CategoryResponse toResponse (Category ca){
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(ca.getId());
        categoryResponse.setName(ca.getName());
        categoryResponse.setDescription(ca.getDescription());
        return categoryResponse;
    }
    public void updateEntity (Category category, CategoryRequest categoryRequest){
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
    }
}
