package com.kedada.backend.metric.service;

import com.kedada.backend.event.service.EventService;
import com.kedada.backend.metric.dto.EventMetricDailyResponse;
import com.kedada.backend.metric.dto.EventMetricSummaryResponse;
import com.kedada.backend.metric.entity.EventMetricDaily;
import com.kedada.backend.metric.repository.EventMetricDailyRepository;
import com.kedada.backend.metric.repository.EventMetricSummaryProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class EventMetricService {

    private final EventMetricDailyRepository repository;
    private final EventService eventService;

    public EventMetricService(EventMetricDailyRepository repository, EventService eventService) {
        this.repository = repository;
        this.eventService = eventService;
    }

    @Transactional
    public void registerView(UUID eventId, UUID ownerId) {
        eventService.findActive(eventId);
        repository.incrementViews(eventId, LocalDate.now(), ownerId);
    }

    @Transactional
    public void registerShare(UUID eventId, UUID ownerId) {
        eventService.findActive(eventId);
        repository.incrementShares(eventId, LocalDate.now(), ownerId);
    }

    @Transactional(readOnly = true)
    public List<EventMetricDailyResponse> daily(UUID eventId) {
        eventService.findActive(eventId);
        return repository.findByIdEventIdOrderByIdDayDesc(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventMetricSummaryResponse summary(UUID eventId) {
        eventService.findActive(eventId);
        EventMetricSummaryProjection projection = repository.summarize(eventId);
        return new EventMetricSummaryResponse(eventId, projection.getViews(), projection.getShares());
    }

    private EventMetricDailyResponse toResponse(EventMetricDaily metric) {
        return new EventMetricDailyResponse(
                metric.getId().getEventId(),
                metric.getId().getDay(),
                metric.getViews(),
                metric.getShares(),
                metric.getOwnerId()
        );
    }
}
