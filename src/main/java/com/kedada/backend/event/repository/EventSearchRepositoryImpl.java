package com.kedada.backend.event.repository;

import com.kedada.backend.event.entity.Event;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventSearchRepositoryImpl implements EventSearchRepository {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "createdAt", "e.created_at",
            "updatedAt", "e.updated_at",
            "title", "e.title",
            "priority", "e.priority",
            "price", "e.price"
    );

    private final EntityManager entityManager;

    public EventSearchRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Page<Event> search(String q, UUID categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer priority,
                              OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable) {
        Map<String, Object> params = new HashMap<>();
        String where = whereClause(q, categoryId, minPrice, maxPrice, priority, fromDate, toDate, params);

        Query contentQuery = entityManager.createNativeQuery("""
                select distinct e.*
                from events e
                """ + where + orderBy(pageable.getSort()), Event.class);
        params.forEach(contentQuery::setParameter);
        contentQuery.setFirstResult((int) pageable.getOffset());
        contentQuery.setMaxResults(pageable.getPageSize());

        Query countQuery = entityManager.createNativeQuery("""
                select count(distinct e.id)
                from events e
                """ + where);
        params.forEach(countQuery::setParameter);

        @SuppressWarnings("unchecked")
        List<Event> events = contentQuery.getResultList();
        long total = ((Number) countQuery.getSingleResult()).longValue();
        return new PageImpl<>(events, pageable, total);
    }

    private String whereClause(String q, UUID categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer priority,
                               OffsetDateTime fromDate, OffsetDateTime toDate, Map<String, Object> params) {
        StringBuilder where = new StringBuilder(" where e.is_deleted = false");
        if (q != null) {
            where.append(" and (e.search_vector @@ plainto_tsquery('spanish', :q) or e.search_vector @@ plainto_tsquery('simple', :q))");
            params.put("q", q);
        }
        if (categoryId != null) {
            where.append(" and exists (select 1 from event_categories ec where ec.event_id = e.id and ec.category_id = :categoryId)");
            params.put("categoryId", categoryId);
        }
        if (minPrice != null) {
            where.append(" and e.price >= :minPrice");
            params.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            where.append(" and e.price <= :maxPrice");
            params.put("maxPrice", maxPrice);
        }
        if (priority != null) {
            where.append(" and e.priority = :priority");
            params.put("priority", priority);
        }
        if (fromDate != null) {
            where.append(" and exists (select 1 from schedules s where s.event_id = e.id and s.start_date >= :fromDate)");
            params.put("fromDate", fromDate);
        }
        if (toDate != null) {
            where.append(" and exists (select 1 from schedules s where s.event_id = e.id and s.start_date <= :toDate)");
            params.put("toDate", toDate);
        }
        return where.toString();
    }

    private String orderBy(Sort sort) {
        if (sort.isUnsorted()) {
            return " order by e.created_at desc, e.id asc";
        }

        StringBuilder orderBy = new StringBuilder(" order by ");
        boolean first = true;
        for (Sort.Order order : sort) {
            String column = SORT_COLUMNS.get(order.getProperty());
            if (column == null) {
                throw new IllegalArgumentException("Unsupported event sort field: " + order.getProperty());
            }
            if (!first) {
                orderBy.append(", ");
            }
            orderBy.append(column).append(order.isDescending() ? " desc" : " asc");
            first = false;
        }
        return orderBy.append(", e.id asc").toString();
    }
}
