package com.kedada.backend.url.service;

import com.kedada.backend.common.exception.BusinessConflictException;
import com.kedada.backend.common.exception.ResourceNotFoundException;
import com.kedada.backend.event.repository.EventRepository;
import com.kedada.backend.url.dto.UrlCreateRequest;
import com.kedada.backend.url.dto.UrlResponse;
import com.kedada.backend.url.entity.Url;
import com.kedada.backend.url.mapper.UrlMapper;
import com.kedada.backend.url.repository.UrlRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UrlService {

    private final UrlRepository repository;
    private final EventRepository eventRepository;
    private final UrlMapper mapper;

    public UrlService(UrlRepository repository, EventRepository eventRepository, UrlMapper mapper) {
        this.repository = repository;
        this.eventRepository = eventRepository;
        this.mapper = mapper;
    }

    @Transactional
    public UrlResponse create(UrlCreateRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional(readOnly = true)
    public UrlResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public Page<UrlResponse> list(Pageable pageable) {
        return repository.findByDeletedFalse(pageable).map(mapper::toResponse);
    }

    @Transactional
    public UrlResponse update(UUID id, UrlCreateRequest request) {
        Url url = find(id);
        mapper.apply(url, request);
        return mapper.toResponse(url);
    }

    @Transactional
    public void delete(UUID id) {
        Url url = find(id);
        if (eventRepository.existsActiveByUrlId(id)) {
            throw new BusinessConflictException("Url is referenced by at least one active event");
        }
        url.setDeleted(true);
        url.setDeletedAt(OffsetDateTime.now());
    }

    public Url find(UUID id) {
        return repository.findByIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Url not found: " + id));
    }
}
