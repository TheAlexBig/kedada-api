package com.kedada.backend.metric.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class EventMetricDailyId implements Serializable {

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "day")
    private LocalDate day;

    protected EventMetricDailyId() {
    }

    public EventMetricDailyId(UUID eventId, LocalDate day) {
        this.eventId = eventId;
        this.day = day;
    }

    public UUID getEventId() {
        return eventId;
    }

    public LocalDate getDay() {
        return day;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventMetricDailyId that)) {
            return false;
        }
        return Objects.equals(eventId, that.eventId) && Objects.equals(day, that.day);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, day);
    }
}
