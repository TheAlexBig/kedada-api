package com.kedada.backend.event.repository;

import com.kedada.backend.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID>, EventSearchRepository {

    Optional<Event> findByIdAndDeletedFalse(UUID id);

    @Query("select count(e) > 0 from Event e where e.deleted = false and e.type.id = :categoryId")
    boolean existsActiveByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("""
            select count(e) > 0
            from Event e
            where e.deleted = false
              and (e.siteUrl.id = :urlId or e.referenceUrl.id = :urlId)
            """)
    boolean existsActiveByUrlId(@Param("urlId") UUID urlId);

}
