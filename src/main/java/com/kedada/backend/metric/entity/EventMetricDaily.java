package com.kedada.backend.metric.entity;

import com.kedada.backend.event.entity.Event;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "event_metric_daily")
public class EventMetricDaily {

    @EmbeddedId
    private EventMetricDailyId id;

    @MapsId("eventId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private long views;

    @Column(nullable = false)
    private long shares;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    protected EventMetricDaily() {
    }

    public EventMetricDailyId getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public long getViews() {
        return views;
    }

    public long getShares() {
        return shares;
    }

    public UUID getOwnerId() {
        return ownerId;
    }
}
