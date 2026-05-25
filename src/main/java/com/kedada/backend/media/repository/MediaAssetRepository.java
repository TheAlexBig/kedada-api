package com.kedada.backend.media.repository;

import com.kedada.backend.media.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
}
