package com.kedada.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kedada.storage.s3")
public record S3StorageProperties(
        String bucket,
        String region,
        String endpoint,
        String publicEndpoint,
        String accessKey,
        String secretKey,
        boolean pathStyleAccessEnabled,
        long signedUrlExpirationSeconds,
        long maxUploadBytes
) {
}
