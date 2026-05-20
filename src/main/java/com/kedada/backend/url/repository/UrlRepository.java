package com.kedada.backend.url.repository;

import com.kedada.backend.url.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UrlRepository extends JpaRepository<Url, UUID> {
}
