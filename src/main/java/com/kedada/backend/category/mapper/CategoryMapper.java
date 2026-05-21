package com.kedada.backend.category.mapper;

import com.kedada.backend.category.dto.CategoryCreateRequest;
import com.kedada.backend.category.dto.CategoryResponse;
import com.kedada.backend.category.entity.Category;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryCreateRequest request) {
        Category category = new Category();
        apply(category, request);
        return category;
    }

    public void apply(Category category, CategoryCreateRequest request) {
        category.setName(request.name());
        category.setType(request.type() == null ? null : request.type().toArray(String[]::new));
    }

    public CategoryResponse toResponse(Category category) {
        List<String> type = category.getType() == null ? null : Arrays.asList(category.getType());
        return new CategoryResponse(category.getId(), category.getName(), category.getOwnerId(), type);
    }
}
