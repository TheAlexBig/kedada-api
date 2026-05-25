package com.kedada.backend.media.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MediaAssetResponse(
        UUID id,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        String readUrl,
        OffsetDateTime readUrlExpiresAt,
        OffsetDateTime createdAt
) {
}
