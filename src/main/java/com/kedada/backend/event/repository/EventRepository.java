package com.kedada.backend.event.repository;

import com.kedada.backend.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID>, EventSearchRepository {

    Optional<Event> findByIdAndDeletedFalse(UUID id);

    boolean existsByType_Id(UUID categoryId);

    boolean existsBySiteUrl_IdOrReferenceUrl_Id(UUID siteUrlId, UUID referenceUrlId);

}
