package com.kedada.backend.event.dto;

import jakarta.validation.constraints.NotNull;

public record EventVisibilityRequest(
        @NotNull Boolean visibleOnWebsite
) {
}
