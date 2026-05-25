package com.kedada.backend.url.repository;

import com.kedada.backend.url.entity.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UrlRepository extends JpaRepository<Url, UUID> {

    Optional<Url> findByIdAndDeletedFalse(UUID id);

    @Query("""
            select u from Url u left join u.event e
            where u.deleted = false
              and (u.event is null or (e.deleted = false and (e.visibleOnWebsite = true or :requesterId is not null)))
            """)
    Page<Url> findAccessible(@Param("requesterId") UUID requesterId, Pageable pageable);

    Page<Url> findByEvent_IdAndDeletedFalse(UUID eventId, Pageable pageable);
}
