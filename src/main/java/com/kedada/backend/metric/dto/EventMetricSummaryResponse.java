package com.kedada.backend.metric.dto;

import java.util.UUID;

public record EventMetricSummaryResponse(
        UUID eventId,
        long views,
        long shares
) {
}
