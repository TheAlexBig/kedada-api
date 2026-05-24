package com.kedada.backend.url.repository;

import com.kedada.backend.url.entity.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UrlRepository extends JpaRepository<Url, UUID> {

    Optional<Url> findByIdAndDeletedFalse(UUID id);

    Page<Url> findByDeletedFalse(Pageable pageable);

    Page<Url> findByEvent_IdAndDeletedFalse(UUID eventId, Pageable pageable);
}
