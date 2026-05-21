package com.kedada.backend.event.controller;

import com.kedada.backend.auth.security.AuthenticatedUser;
import com.kedada.backend.event.dto.EventCreateRequest;
import com.kedada.backend.event.dto.EventResponse;
import com.kedada.backend.event.dto.EventUpdateRequest;
import com.kedada.backend.event.service.EventService;
import com.kedada.backend.metric.service.EventMetricService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;
    private final EventMetricService metricService;

    public EventController(EventService eventService, EventMetricService metricService) {
        this.eventService = eventService;
        this.metricService = metricService;
    }

    @PostMapping
    ResponseEntity<EventResponse> create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody EventCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(user.id(), request));
    }

    @GetMapping("/{id}")
    EventResponse get(@PathVariable UUID id) {
        return eventService.get(id);
    }

    @GetMapping
    Page<EventResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) @Min(1) Integer priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return eventService.search(q, categoryId, minPrice, maxPrice, priority, fromDate, toDate, pageable);
    }

    @PutMapping("/{id}")
    EventResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id, @Valid @RequestBody EventUpdateRequest request) {
        return eventService.update(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        eventService.softDelete(user.id(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/view")
    ResponseEntity<Void> view(@PathVariable UUID id, @RequestParam @NotNull UUID ownerId) {
        metricService.registerView(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/share")
    ResponseEntity<Void> share(@PathVariable UUID id, @RequestParam @NotNull UUID ownerId) {
        metricService.registerShare(id, ownerId);
        return ResponseEntity.noContent().build();
    }
}
