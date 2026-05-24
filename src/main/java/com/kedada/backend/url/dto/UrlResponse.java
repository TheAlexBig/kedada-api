package com.kedada.backend.url.dto;

import java.util.UUID;

public record UrlResponse(
        UUID id,
        UUID eventId,
        String url,
        String description,
        UUID ownerId,
        String kind
) {
}
