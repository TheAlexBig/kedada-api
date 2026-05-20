package com.kedada.backend.metric.controller;

import com.kedada.backend.metric.dto.EventMetricDailyResponse;
import com.kedada.backend.metric.dto.EventMetricSummaryResponse;
import com.kedada.backend.metric.service.EventMetricService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events/{eventId}/metrics")
public class EventMetricController {

    private final EventMetricService service;

    public EventMetricController(EventMetricService service) {
        this.service = service;
    }

    @GetMapping("/daily")
    List<EventMetricDailyResponse> daily(@PathVariable UUID eventId) {
        return service.daily(eventId);
    }

    @GetMapping("/summary")
    EventMetricSummaryResponse summary(@PathVariable UUID eventId) {
        return service.summary(eventId);
    }
}
