package com.kedada.backend.metric.controller;

import com.kedada.backend.auth.security.AuthenticatedUser;
import com.kedada.backend.metric.dto.EventMetricDailyResponse;
import com.kedada.backend.metric.dto.EventMetricSummaryResponse;
import com.kedada.backend.metric.service.EventMetricService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
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
    List<EventMetricDailyResponse> daily(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID eventId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        if (user == null) {
            throw new AccessDeniedException("Authentication is required to view daily metrics");
        }
        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.minusDays(29) : from;
        return service.daily(eventId, requesterId(user), start, end);
    }

    @GetMapping("/summary")
    EventMetricSummaryResponse summary(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID eventId) {
        return service.summary(eventId, requesterId(user));
    }

    private UUID requesterId(AuthenticatedUser user) {
        return user == null ? null : user.id();
    }
}
