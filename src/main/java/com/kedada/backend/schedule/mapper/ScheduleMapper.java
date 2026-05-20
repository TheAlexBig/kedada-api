package com.kedada.backend.schedule.mapper;

import com.kedada.backend.schedule.dto.ScheduleResponse;
import com.kedada.backend.schedule.entity.Schedule;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ScheduleMapper {

    public ScheduleResponse toResponse(Schedule schedule) {
        UUID eventId = schedule.getEvent() == null ? null : schedule.getEvent().getId();
        return new ScheduleResponse(
                schedule.getId(),
                eventId,
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getOwnerId()
        );
    }
}
