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
    public ScheduleResponse create(ScheduleCreateRequest request) {
        Schedule schedule = new Schedule();
        apply(schedule, request);
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
    public ScheduleResponse update(UUID id, ScheduleCreateRequest request) {
        Schedule schedule = find(id);
        apply(schedule, request);
        return mapper.toResponse(schedule);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    private void apply(Schedule schedule, ScheduleCreateRequest request) {
        Event event = request.eventId() == null ? null : eventService.findActive(request.eventId());
        schedule.setEvent(event);
        schedule.setStartDate(request.startDate());
        schedule.setEndDate(request.endDate());
        schedule.setOwnerId(request.ownerId());
    }

    private Schedule find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + id));
    }
}
