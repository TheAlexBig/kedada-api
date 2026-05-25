package com.kedada.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3StorageConfig {

    @Bean
    S3Client s3Client(S3StorageProperties properties) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider(properties))
                .forcePathStyle(properties.pathStyleAccessEnabled());

        if (hasText(properties.endpoint())) {
            builder.endpointOverride(URI.create(properties.endpoint()));
        }
        return builder.build();
    }

    @Bean
    S3Presigner s3Presigner(S3StorageProperties properties) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider(properties))
                .serviceConfiguration(serviceConfiguration(properties));

        if (hasText(properties.publicEndpoint())) {
            builder.endpointOverride(URI.create(properties.publicEndpoint()));
        } else if (hasText(properties.endpoint())) {
            builder.endpointOverride(URI.create(properties.endpoint()));
        }
        return builder.build();
    }

    private AwsCredentialsProvider credentialsProvider(S3StorageProperties properties) {
        if (hasText(properties.accessKey()) && hasText(properties.secretKey())) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.accessKey(), properties.secretKey()));
        }
        return DefaultCredentialsProvider.builder().build();
    }

    private S3Configuration serviceConfiguration(S3StorageProperties properties) {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(properties.pathStyleAccessEnabled())
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
