package com.kedada.backend.url.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record UrlCreateRequest(
        @NotBlank @URL String url,
        @Size(max = 100) String description,
        @NotBlank @Size(max = 20) String kind
) {
}
