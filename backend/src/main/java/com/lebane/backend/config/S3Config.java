package com.lebane.backend.config;

import com.lebane.backend.storage.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class S3Config {
    @Bean
    public S3Client s3Client(StorageProperties properties) {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(properties.accessKey(), properties.secretKey());

        return S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(StorageProperties properties) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider(properties))
                .serviceConfiguration(s3Configuration())
                .build();
    }

    private StaticCredentialsProvider credentialsProvider(StorageProperties properties) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(properties.accessKey(), properties.secretKey());

        return StaticCredentialsProvider.create(credentials);
    }

    private S3Configuration s3Configuration() {
        return S3Configuration.builder().pathStyleAccessEnabled(true).build();
    }
}
