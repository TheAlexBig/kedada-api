package com.kedada.backend.url.service;

import com.kedada.backend.common.exception.ResourceNotFoundException;
import com.kedada.backend.event.entity.Event;
import com.kedada.backend.event.service.EventService;
import com.kedada.backend.url.dto.UrlCreateRequest;
import com.kedada.backend.url.dto.UrlResponse;
import com.kedada.backend.url.entity.Url;
import com.kedada.backend.url.mapper.UrlMapper;
import com.kedada.backend.url.repository.UrlRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UrlService {

    private final UrlRepository repository;
    private final EventService eventService;
    private final UrlMapper mapper;

    public UrlService(UrlRepository repository, EventService eventService, UrlMapper mapper) {
        this.repository = repository;
        this.eventService = eventService;
        this.mapper = mapper;
    }

    @Transactional
    public UrlResponse create(UUID ownerId, UrlCreateRequest request) {
        Url url = mapper.toEntity(request);
        url.setEvent(resolveEvent(ownerId, request.eventId()));
        url.setOwnerId(ownerId);
        return mapper.toResponse(repository.save(url));
    }

    @Transactional(readOnly = true)
    public UrlResponse get(UUID id, UUID requesterId) {
        Url url = find(id);
        assertEventAccessible(url, requesterId);
        return mapper.toResponse(url);
    }

    @Transactional(readOnly = true)
    public Page<UrlResponse> list(UUID eventId, UUID requesterId, Pageable pageable) {
        if (eventId == null) {
            return repository.findAccessible(requesterId, pageable).map(mapper::toResponse);
        }

        eventService.findAccessible(eventId, requesterId);
        return repository.findByEvent_IdAndDeletedFalse(eventId, pageable).map(mapper::toResponse);
    }

    @Transactional
    public UrlResponse update(UUID ownerId, UUID id, UrlCreateRequest request) {
        Url url = find(id);
        assertOwner(url.getOwnerId(), ownerId);
        mapper.apply(url, request);
        url.setEvent(resolveEvent(ownerId, request.eventId()));
        return mapper.toResponse(url);
    }

    @Transactional
    public void delete(UUID ownerId, UUID id) {
        Url url = find(id);
        assertOwner(url.getOwnerId(), ownerId);
        url.setDeleted(true);
        url.setDeletedAt(OffsetDateTime.now());
    }

    public Url find(UUID id) {
        return repository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Url not found: " + id));
    }

    private Event resolveEvent(UUID ownerId, UUID eventId) {
        return eventId == null ? null : eventService.findOwnedActive(ownerId, eventId);
    }

    private void assertEventAccessible(Url url, UUID requesterId) {
        if (url.getEvent() != null) {
            eventService.findAccessible(url.getEvent().getId(), requesterId);
        }
    }

    private void assertOwner(UUID actualOwnerId, UUID expectedOwnerId) {
        if (!actualOwnerId.equals(expectedOwnerId)) {
            throw new AccessDeniedException("You do not own this URL");
        }
    }
}
