package com.kedada.backend.event.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record EventUpdateRequest(
        @Size(max = 100) String title,
        String description,
        @Min(1) Integer priority,
        UUID thumbnail,
        @DecimalMin("0.00") BigDecimal price,
        UUID siteUrlId,
        UUID referenceUrlId,
        UUID categoryId
) {
}
