package com.kedada.backend.url.mapper;

import com.kedada.backend.url.dto.UrlCreateRequest;
import com.kedada.backend.url.dto.UrlResponse;
import com.kedada.backend.url.entity.Url;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UrlMapper {

    public Url toEntity(UrlCreateRequest request) {
        Url url = new Url();
        apply(url, request);
        return url;
    }

    public void apply(Url url, UrlCreateRequest request) {
        url.setUrl(request.url());
        url.setDescription(request.description());
        url.setKind(request.kind());
    }

    public UrlResponse toResponse(Url url) {
        UUID eventId = url.getEvent() == null ? null : url.getEvent().getId();
        return new UrlResponse(url.getId(), eventId, url.getUrl(), url.getDescription(), url.getOwnerId(), url.getKind());
    }
}
