package com.kedada.backend.event.mapper;

import com.kedada.backend.event.dto.EventResponse;
import com.kedada.backend.event.entity.Event;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EventMapper {

    public EventResponse toResponse(Event event) {
        UUID siteUrlId = event.getSiteUrl() == null ? null : event.getSiteUrl().getId();
        UUID referenceUrlId = event.getReferenceUrl() == null ? null : event.getReferenceUrl().getId();
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getPriority(),
                event.getThumbnail(),
                event.getPrice(),
                siteUrlId,
                referenceUrlId,
                event.getType().getId(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
