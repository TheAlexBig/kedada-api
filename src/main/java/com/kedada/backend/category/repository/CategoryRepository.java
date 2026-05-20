package com.kedada.backend.category.repository;

import com.kedada.backend.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByIdAndDeletedFalse(UUID id);

    Page<Category> findByDeletedFalse(Pageable pageable);
}
