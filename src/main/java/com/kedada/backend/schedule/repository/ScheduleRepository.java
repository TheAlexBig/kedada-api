package com.kedada.backend.schedule.repository;

import com.kedada.backend.schedule.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    @Query("""
            select s from Schedule s left join s.event e
            where s.event is null or (e.deleted = false and (e.visibleOnWebsite = true or :requesterId is not null))
            """)
    Page<Schedule> findAccessible(@Param("requesterId") UUID requesterId, Pageable pageable);

    Page<Schedule> findByEvent_Id(UUID eventId, Pageable pageable);
}
