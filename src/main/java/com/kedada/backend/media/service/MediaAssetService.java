package com.kedada.backend.media.service;

import com.kedada.backend.common.exception.ResourceNotFoundException;
import com.kedada.backend.config.S3StorageProperties;
import com.kedada.backend.event.repository.EventRepository;
import com.kedada.backend.media.dto.MediaAssetResponse;
import com.kedada.backend.media.entity.MediaAsset;
import com.kedada.backend.media.repository.MediaAssetRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaAssetService {

    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final MediaAssetRepository repository;
    private final EventRepository eventRepository;
    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final S3StorageProperties properties;

    public MediaAssetService(MediaAssetRepository repository, EventRepository eventRepository, S3Client s3Client, S3Presigner presigner, S3StorageProperties properties) {
        this.repository = repository;
        this.eventRepository = eventRepository;
        this.s3Client = s3Client;
        this.presigner = presigner;
        this.properties = properties;
    }

    @Transactional
    public MediaAssetResponse upload(UUID ownerId, MultipartFile file) {
        validateImage(file);
        String objectKey = "event-images/" + ownerId + "/" + UUID.randomUUID();
        MediaAsset asset = new MediaAsset();
        asset.setObjectKey(objectKey);
        asset.setOriginalFilename(safeFilename(file.getOriginalFilename()));
        asset.setContentType(file.getContentType());
        asset.setSizeBytes(file.getSize());
        asset.setBucket(properties.bucket());
        asset.setOwnerId(ownerId);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(properties.bucket())
                            .key(objectKey)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException | S3Exception ex) {
            throw new IllegalStateException("Unable to store image", ex);
        }

        return toResponse(repository.save(asset));
    }

    @Transactional(readOnly = true)
    public MediaAssetResponse get(UUID id, UUID requesterId) {
        MediaAsset asset = find(id);
        boolean isPublished = eventRepository.existsVisibleByThumbnailId(id);
        boolean isUsedByActiveEvent = requesterId != null && eventRepository.existsActiveByThumbnailId(id);
        boolean isOwner = requesterId != null && requesterId.equals(asset.getOwnerId());
        if (!isPublished && !isUsedByActiveEvent && !isOwner) {
            throw new ResourceNotFoundException("Image not found: " + id);
        }
        return toResponse(asset);
    }

    @Transactional(readOnly = true)
    public MediaAsset findOwned(UUID ownerId, UUID id) {
        MediaAsset asset = find(id);
        if (!asset.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("You do not own this image");
        }
        return asset;
    }

    private MediaAsset find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + id));
    }

    private MediaAssetResponse toResponse(MediaAsset asset) {
        long expirationSeconds = properties.signedUrlExpirationSeconds();
        String signedUrl = presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofSeconds(expirationSeconds))
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(asset.getBucket())
                                .key(asset.getObjectKey())
                                .build())
                        .build()
        ).url().toString();

        return new MediaAssetResponse(
                asset.getId(),
                asset.getOriginalFilename(),
                asset.getContentType(),
                asset.getSizeBytes(),
                signedUrl,
                OffsetDateTime.now().plusSeconds(expirationSeconds),
                asset.getCreatedAt()
        );
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        if (!SUPPORTED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Supported image types are JPEG, PNG, WEBP, and GIF");
        }
        if (file.getSize() > properties.maxUploadBytes()) {
            throw new IllegalArgumentException("Image exceeds the maximum upload size");
        }
        if (!hasExpectedSignature(file)) {
            throw new IllegalArgumentException("File content does not match its image type");
        }
    }

    private boolean hasExpectedSignature(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(12);
            return switch (file.getContentType()) {
                case "image/jpeg" -> header.length >= 3 && byteValue(header[0]) == 0xff && byteValue(header[1]) == 0xd8 && byteValue(header[2]) == 0xff;
                case "image/png" -> header.length >= 8 && byteValue(header[0]) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G';
                case "image/gif" -> header.length >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F';
                case "image/webp" -> header.length >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                        && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
                default -> false;
            };
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read image file", ex);
        }
    }

    private int byteValue(byte value) {
        return value & 0xff;
    }

    private String safeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "image";
        }
        String normalized = originalFilename.replace("\\", "/");
        String filename = normalized.substring(normalized.lastIndexOf('/') + 1);
        return filename.length() > 255 ? filename.substring(filename.length() - 255) : filename;
    }
}
