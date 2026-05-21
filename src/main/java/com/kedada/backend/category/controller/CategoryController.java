package com.kedada.backend.category.controller;

import com.kedada.backend.auth.security.AuthenticatedUser;
import com.kedada.backend.category.dto.CategoryCreateRequest;
import com.kedada.backend.category.dto.CategoryResponse;
import com.kedada.backend.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<CategoryResponse> create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(user.id(), request));
    }

    @GetMapping
    Page<CategoryResponse> list(@ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    CategoryResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    CategoryResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id, @Valid @RequestBody CategoryCreateRequest request) {
        return service.update(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        service.delete(user.id(), id);
        return ResponseEntity.noContent().build();
    }
}
