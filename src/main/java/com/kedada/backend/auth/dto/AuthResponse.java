package com.kedada.backend.auth.dto;

import java.util.UUID;

public record AuthResponse(
        String tokenType,
        String accessToken,
        UUID userId,
        String email,
        String name
) {
}
