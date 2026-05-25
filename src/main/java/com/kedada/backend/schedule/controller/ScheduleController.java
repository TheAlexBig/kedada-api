package com.kedada.backend.schedule.controller;

import com.kedada.backend.auth.security.AuthenticatedUser;
import com.kedada.backend.schedule.dto.ScheduleCreateRequest;
import com.kedada.backend.schedule.dto.ScheduleResponse;
import com.kedada.backend.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService service;

    public ScheduleController(ScheduleService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<ScheduleResponse> create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ScheduleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(user.id(), request));
    }

    @GetMapping("/{id}")
    ScheduleResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        return service.get(id, requesterId(user));
    }

    @GetMapping
    Page<ScheduleResponse> list(@AuthenticationPrincipal AuthenticatedUser user, @RequestParam(required = false) UUID eventId,
                                @ParameterObject @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        return service.list(eventId, requesterId(user), pageable);
    }

    @PutMapping("/{id}")
    ScheduleResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id, @Valid @RequestBody ScheduleCreateRequest request) {
        return service.update(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        service.delete(user.id(), id);
        return ResponseEntity.noContent().build();
    }

    private UUID requesterId(AuthenticatedUser user) {
        return user == null ? null : user.id();
    }
}
