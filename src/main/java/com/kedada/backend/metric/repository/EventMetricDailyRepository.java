package com.kedada.backend.metric.repository;

import com.kedada.backend.metric.entity.EventMetricDaily;
import com.kedada.backend.metric.entity.EventMetricDailyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventMetricDailyRepository extends JpaRepository<EventMetricDaily, EventMetricDailyId> {

    List<EventMetricDaily> findByIdEventIdOrderByIdDayDesc(UUID eventId);

    @Modifying
    @Query(value = """
            insert into event_metric_daily(event_id, day, views, shares, owner_id)
            values (:eventId, :day, 1, 0, :ownerId)
            on conflict (event_id, day)
            do update set views = event_metric_daily.views + 1
            """, nativeQuery = true)
    int incrementViews(@Param("eventId") UUID eventId, @Param("day") LocalDate day, @Param("ownerId") UUID ownerId);

    @Modifying
    @Query(value = """
            insert into event_metric_daily(event_id, day, views, shares, owner_id)
            values (:eventId, :day, 0, 1, :ownerId)
            on conflict (event_id, day)
            do update set shares = event_metric_daily.shares + 1
            """, nativeQuery = true)
    int incrementShares(@Param("eventId") UUID eventId, @Param("day") LocalDate day, @Param("ownerId") UUID ownerId);

    @Query(value = """
            select coalesce(sum(views), 0) as views, coalesce(sum(shares), 0) as shares
            from event_metric_daily
            where event_id = :eventId
            """, nativeQuery = true)
    EventMetricSummaryProjection summarize(@Param("eventId") UUID eventId);
}
