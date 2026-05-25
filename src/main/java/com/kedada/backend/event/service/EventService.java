package com.kedada.backend.event.service;

import com.kedada.backend.category.entity.Category;
import com.kedada.backend.category.service.CategoryService;
import com.kedada.backend.common.exception.ResourceNotFoundException;
import com.kedada.backend.event.dto.EventCreateRequest;
import com.kedada.backend.event.dto.EventResponse;
import com.kedada.backend.event.dto.EventUpdateRequest;
import com.kedada.backend.event.entity.Event;
import com.kedada.backend.event.mapper.EventMapper;
import com.kedada.backend.event.repository.EventRepository;
import com.kedada.backend.media.service.MediaAssetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository repository;
    private final CategoryService categoryService;
    private final MediaAssetService mediaAssetService;
    private final EventMapper mapper;

    public EventService(EventRepository repository, CategoryService categoryService, MediaAssetService mediaAssetService, EventMapper mapper) {
        this.repository = repository;
        this.categoryService = categoryService;
        this.mediaAssetService = mediaAssetService;
        this.mapper = mapper;
    }

    @Transactional
    public EventResponse create(UUID ownerId, EventCreateRequest request) {
        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setPriority(request.priority() == null ? 1 : request.priority());
        event.setThumbnail(resolveThumbnail(ownerId, request.thumbnail()));
        event.setPrice(request.price());
        event.setVisibleOnWebsite(request.visibleOnWebsite() == null || request.visibleOnWebsite());
        event.setCategories(resolveCategories(request.categoryIds()));
        event.setOwnerId(ownerId);
        return mapper.toResponse(repository.save(event));
    }

    @Transactional(readOnly = true)
    public EventResponse get(UUID id, UUID requesterId) {
        return mapper.toResponse(findAccessible(id, requesterId));
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> search(String q, UUID categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer priority,
                                      OffsetDateTime fromDate, OffsetDateTime toDate, UUID requesterId, Pageable pageable) {
        return repository.search(blankToNull(q), categoryId, minPrice, maxPrice, priority, fromDate, toDate, requesterId, pageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public EventResponse update(UUID ownerId, UUID id, EventUpdateRequest request) {
        Event event = findActive(id);
        assertOwner(event.getOwnerId(), ownerId);
        if (request.title() != null) {
            event.setTitle(request.title());
        }
        if (request.description() != null) {
            event.setDescription(request.description());
        }
        if (request.priority() != null) {
            event.setPriority(request.priority());
        }
        event.setThumbnail(resolveThumbnail(ownerId, request.thumbnail()));
        if (request.price() != null) {
            event.setPrice(request.price());
        }
        if (request.visibleOnWebsite() != null) {
            event.setVisibleOnWebsite(request.visibleOnWebsite());
        }
        if (request.categoryIds() != null) {
            event.setCategories(resolveCategories(request.categoryIds()));
        }
        return mapper.toResponse(event);
    }

    @Transactional
    public EventResponse updateVisibility(UUID id, boolean visibleOnWebsite) {
        Event event = findActive(id);
        event.setVisibleOnWebsite(visibleOnWebsite);
        return mapper.toResponse(event);
    }

    @Transactional
    public void softDelete(UUID ownerId, UUID id) {
        Event event = findActive(id);
        assertOwner(event.getOwnerId(), ownerId);
        event.setDeleted(true);
        event.setDeletedAt(OffsetDateTime.now());
    }

    @Transactional(readOnly = true)
    public Event findActive(UUID id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }

    public Event findOwnedActive(UUID ownerId, UUID id) {
        Event event = findActive(id);
        assertOwner(event.getOwnerId(), ownerId);
        return event;
    }

    @Transactional(readOnly = true)
    public Event findAccessible(UUID id, UUID requesterId) {
        Event event = findActive(id);
        if (!event.isVisibleOnWebsite() && requesterId == null) {
            throw new ResourceNotFoundException("Event not found: " + id);
        }
        return event;
    }

    @Transactional(readOnly = true)
    public Event findVisibleOnWebsite(UUID id) {
        return findAccessible(id, null);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private LinkedHashSet<Category> resolveCategories(List<UUID> categoryIds) {
        return categoryIds.stream()
                .map(categoryService::find)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private UUID resolveThumbnail(UUID ownerId, UUID thumbnailId) {
        if (thumbnailId == null) {
            return null;
        }
        mediaAssetService.findOwned(ownerId, thumbnailId);
        return thumbnailId;
    }

    private void assertOwner(UUID actualOwnerId, UUID expectedOwnerId) {
        if (!actualOwnerId.equals(expectedOwnerId)) {
            throw new AccessDeniedException("You do not own this event");
        }
    }
}
