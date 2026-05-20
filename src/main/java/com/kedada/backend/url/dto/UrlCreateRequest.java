package com.kedada.backend.url.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

public record UrlCreateRequest(
        @NotBlank @URL String url,
        @Size(max = 100) String description,
        @NotNull UUID ownerId,
        @NotBlank @Size(max = 20) String kind
) {
}
