package com.kedada.backend.event.repository;

import com.kedada.backend.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID>, EventSearchRepository {

    Optional<Event> findByIdAndDeletedFalse(UUID id);

    @Query("select count(e) > 0 from Event e join e.categories c where e.deleted = false and c.id = :categoryId")
    boolean existsActiveByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("select count(e) > 0 from Event e where e.deleted = false and e.visibleOnWebsite = true and e.thumbnail = :mediaId")
    boolean existsVisibleByThumbnailId(@Param("mediaId") UUID mediaId);

    @Query("select count(e) > 0 from Event e where e.deleted = false and e.thumbnail = :mediaId")
    boolean existsActiveByThumbnailId(@Param("mediaId") UUID mediaId);

}
