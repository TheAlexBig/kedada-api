package com.kedada.backend.schedule.repository;

import com.kedada.backend.schedule.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    Page<Schedule> findByEvent_Id(UUID eventId, Pageable pageable);
}
