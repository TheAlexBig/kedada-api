package com.kedada.backend.category.dto;

import java.util.List;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        UUID ownerId,
        List<String> type
) {
}
