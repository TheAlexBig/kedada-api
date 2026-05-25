package com.kedada.backend.event.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        String description,
        Integer priority,
        UUID thumbnail,
        BigDecimal price,
        boolean visibleOnWebsite,
        List<UUID> categoryIds,
        UUID ownerId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
