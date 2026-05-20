package com.kedada.backend.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CategoryCreateRequest(
        @NotBlank String name,
        @NotNull UUID ownerId,
        List<String> type
) {
}
