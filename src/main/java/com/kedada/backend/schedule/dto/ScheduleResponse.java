package com.kedada.backend.schedule.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScheduleResponse(
        UUID id,
        UUID eventId,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID ownerId
) {
}
