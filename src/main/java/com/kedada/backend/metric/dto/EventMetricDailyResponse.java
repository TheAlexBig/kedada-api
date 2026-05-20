package com.kedada.backend.metric.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EventMetricDailyResponse(
        UUID eventId,
        LocalDate day,
        long views,
        long shares,
        UUID ownerId
) {
}
