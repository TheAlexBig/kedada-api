package com.kedada.backend.event.mapper;

import com.kedada.backend.event.dto.EventResponse;
import com.kedada.backend.event.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getPriority(),
                event.getThumbnail(),
                event.getPrice(),
                event.isVisibleOnWebsite(),
                event.getCategories().stream().map(category -> category.getId()).toList(),
                event.getOwnerId(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
