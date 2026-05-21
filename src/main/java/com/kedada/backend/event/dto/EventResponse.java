package com.kedada.backend.event.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        String description,
        Integer priority,
        UUID thumbnail,
        BigDecimal price,
        UUID siteUrlId,
        UUID referenceUrlId,
        UUID categoryId,
        UUID ownerId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
