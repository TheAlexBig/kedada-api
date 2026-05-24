package com.kedada.backend.event.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EventCreateRequest(
        @NotBlank @Size(max = 100) String title,
        String description,
        @Min(1) Integer priority,
        UUID thumbnail,
        @DecimalMin("0.00") BigDecimal price,
        @NotEmpty List<UUID> categoryIds
) {
}
