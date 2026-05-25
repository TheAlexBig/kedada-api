package com.kedada.backend.metric.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kedada.backend.common.exception.TooManyRequestsException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class EventMetricRequestGuard {

    private static final int MAX_TRACKING_REQUESTS_PER_MINUTE = 60;

    private final Cache<String, AtomicInteger> recentRequestCounts = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterWrite(Duration.ofMinutes(1))
            .build();
    private final Cache<String, Boolean> recentViews = Caffeine.newBuilder()
            .maximumSize(500_000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();
    private final Cache<String, Boolean> recentShares = Caffeine.newBuilder()
            .maximumSize(500_000)
            .expireAfterWrite(Duration.ofMinutes(1))
            .build();

    boolean shouldRecordView(UUID eventId, String clientAddress) {
        assertRateLimit(clientAddress);
        return recentViews.asMap().putIfAbsent(key(eventId, clientAddress), Boolean.TRUE) == null;
    }

    boolean shouldRecordShare(UUID eventId, String clientAddress) {
        assertRateLimit(clientAddress);
        return recentShares.asMap().putIfAbsent(key(eventId, clientAddress), Boolean.TRUE) == null;
    }

    private void assertRateLimit(String clientAddress) {
        AtomicInteger count = recentRequestCounts.get(clientAddress, ignored -> new AtomicInteger());
        if (count.incrementAndGet() > MAX_TRACKING_REQUESTS_PER_MINUTE) {
            throw new TooManyRequestsException("Too many metric tracking requests");
        }
    }

    private String key(UUID eventId, String clientAddress) {
        return eventId + "|" + clientAddress;
    }
}
