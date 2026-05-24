package com.kedada.backend.url.controller;

import com.kedada.backend.auth.security.AuthenticatedUser;
import com.kedada.backend.url.dto.UrlCreateRequest;
import com.kedada.backend.url.dto.UrlResponse;
import com.kedada.backend.url.service.UrlService;
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
@RequestMapping("/api/v1/urls")
public class UrlController {

    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<UrlResponse> create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody UrlCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(user.id(), request));
    }

    @GetMapping("/{id}")
    UrlResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    Page<UrlResponse> list(@RequestParam(required = false) UUID eventId,
                           @ParameterObject @PageableDefault(size = 20, sort = "kind") Pageable pageable) {
        return service.list(eventId, pageable);
    }

    @PutMapping("/{id}")
    UrlResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id, @Valid @RequestBody UrlCreateRequest request) {
        return service.update(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        service.delete(user.id(), id);
        return ResponseEntity.noContent().build();
    }
}
