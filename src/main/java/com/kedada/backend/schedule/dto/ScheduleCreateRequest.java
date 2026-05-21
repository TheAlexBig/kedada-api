package com.kedada.backend.schedule.dto;

import com.kedada.backend.common.validation.DateRangeProvider;
import com.kedada.backend.common.validation.ValidDateRange;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@ValidDateRange
public record ScheduleCreateRequest(
        UUID eventId,
        @NotNull OffsetDateTime startDate,
        OffsetDateTime endDate
) implements DateRangeProvider {
}
