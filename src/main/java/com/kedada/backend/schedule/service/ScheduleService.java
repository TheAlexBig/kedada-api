package com.kedada.backend.schedule.service;

import com.kedada.backend.common.exception.ResourceNotFoundException;
import com.kedada.backend.event.entity.Event;
import com.kedada.backend.event.service.EventService;
import com.kedada.backend.schedule.dto.ScheduleCreateRequest;
import com.kedada.backend.schedule.dto.ScheduleResponse;
import com.kedada.backend.schedule.entity.Schedule;
import com.kedada.backend.schedule.mapper.ScheduleMapper;
import com.kedada.backend.schedule.repository.ScheduleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ScheduleService {

    private final ScheduleRepository repository;
    private final EventService eventService;
    private final ScheduleMapper mapper;

    public ScheduleService(ScheduleRepository repository, EventService eventService, ScheduleMapper mapper) {
        this.repository = repository;
        this.eventService = eventService;
        this.mapper = mapper;
    }

    @Transactional
    public ScheduleResponse create(UUID ownerId, ScheduleCreateRequest request) {
        Schedule schedule = new Schedule();
        apply(schedule, ownerId, request);
        return mapper.toResponse(repository.save(schedule));
    }

    @Transactional(readOnly = true)
    public ScheduleResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public Page<ScheduleResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional
    public ScheduleResponse update(UUID ownerId, UUID id, ScheduleCreateRequest request) {
        Schedule schedule = find(id);
        assertOwner(schedule.getOwnerId(), ownerId);
        apply(schedule, ownerId, request);
        return mapper.toResponse(schedule);
    }

    @Transactional
    public void delete(UUID ownerId, UUID id) {
        Schedule schedule = find(id);
        assertOwner(schedule.getOwnerId(), ownerId);
        repository.delete(schedule);
    }

    private void apply(Schedule schedule, UUID ownerId, ScheduleCreateRequest request) {
        Event event = request.eventId() == null ? null : eventService.findOwnedActive(ownerId, request.eventId());
        schedule.setEvent(event);
        schedule.setStartDate(request.startDate());
        schedule.setEndDate(request.endDate());
        schedule.setOwnerId(ownerId);
    }

    private Schedule find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + id));
    }

    private void assertOwner(UUID actualOwnerId, UUID expectedOwnerId) {
        if (!actualOwnerId.equals(expectedOwnerId)) {
            throw new AccessDeniedException("You do not own this schedule");
        }
    }
}
