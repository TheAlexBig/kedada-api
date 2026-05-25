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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class EventMetricService {

    private final EventMetricDailyRepository repository;
    private final EventService eventService;
    private final EventMetricRequestGuard requestGuard;

    public EventMetricService(EventMetricDailyRepository repository, EventService eventService, EventMetricRequestGuard requestGuard) {
        this.repository = repository;
        this.eventService = eventService;
        this.requestGuard = requestGuard;
    }

    @Transactional
    public void registerView(UUID eventId, String clientAddress) {
        if (!requestGuard.shouldRecordView(eventId, clientAddress)) {
            return;
        }
        var event = eventService.findVisibleOnWebsite(eventId);
        repository.incrementViews(eventId, LocalDate.now(), event.getOwnerId());
    }

    @Transactional
    public void registerShare(UUID eventId, String clientAddress) {
        if (!requestGuard.shouldRecordShare(eventId, clientAddress)) {
            return;
        }
        var event = eventService.findVisibleOnWebsite(eventId);
        repository.incrementShares(eventId, LocalDate.now(), event.getOwnerId());
    }

    @Transactional(readOnly = true)
    public List<EventMetricDailyResponse> daily(UUID eventId, UUID requesterId, LocalDate from, LocalDate to) {
        eventService.findAccessible(eventId, requesterId);
        if (to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > 365) {
            throw new IllegalArgumentException("Metric date range must contain at most 366 days");
        }
        return repository.findByIdEventIdAndIdDayBetweenOrderByIdDayAsc(eventId, from, to).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventMetricSummaryResponse summary(UUID eventId, UUID requesterId) {
        eventService.findAccessible(eventId, requesterId);
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
