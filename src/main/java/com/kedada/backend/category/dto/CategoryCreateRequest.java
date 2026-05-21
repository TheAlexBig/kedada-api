package com.kedada.backend.category.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CategoryCreateRequest(
        @NotBlank String name,
        List<String> type
) {
}
