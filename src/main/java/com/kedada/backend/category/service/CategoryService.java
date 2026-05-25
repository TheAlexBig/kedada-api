package com.kedada.backend.category.service;

import com.kedada.backend.category.dto.CategoryCreateRequest;
import com.kedada.backend.category.dto.CategoryResponse;
import com.kedada.backend.category.entity.Category;
import com.kedada.backend.category.mapper.CategoryMapper;
import com.kedada.backend.category.repository.CategoryRepository;
import com.kedada.backend.common.exception.BusinessConflictException;
import com.kedada.backend.common.exception.ResourceNotFoundException;
import com.kedada.backend.event.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final EventRepository eventRepository;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository repository, EventRepository eventRepository, CategoryMapper mapper) {
        this.repository = repository;
        this.eventRepository = eventRepository;
        this.mapper = mapper;
    }

    @Transactional
    public CategoryResponse create(UUID ownerId, CategoryCreateRequest request) {
        Category category = mapper.toEntity(request);
        category.setOwnerId(ownerId);
        return mapper.toResponse(repository.save(category));
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> list(Pageable pageable) {
        return repository.findByDeletedFalse(pageable).map(mapper::toResponse);
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryCreateRequest request) {
        Category category = find(id);
        mapper.apply(category, request);
        return mapper.toResponse(category);
    }

    @Transactional
    public void delete(UUID id) {
        Category category = find(id);
        if (eventRepository.existsActiveByCategoryId(id)) {
            throw new BusinessConflictException("Category is referenced by at least one active event");
        }
        category.setDeleted(true);
        category.setDeletedAt(OffsetDateTime.now());
    }

    public Category find(UUID id) {
        return repository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }
}
