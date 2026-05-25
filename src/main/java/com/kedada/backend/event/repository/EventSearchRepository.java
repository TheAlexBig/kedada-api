package com.kedada.backend.event.repository;

import com.kedada.backend.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface EventSearchRepository {

    Page<Event> search(
            String q,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer priority,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            UUID requesterId,
            Pageable pageable
    );
}
