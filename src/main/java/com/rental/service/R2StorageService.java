package com.rental.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class R2StorageService {

    @Value("${r2.access.key}")
    private String accessKey;

    @Value("${r2.secret.key}")
    private String secretKey;

    @Value("${r2.endpoint}")
    private String endpoint;

    @Value("${r2.bucket.name}")
    private String bucketName;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    @PostConstruct
    public void init() {
        if (accessKey == null || accessKey.isEmpty() || endpoint == null || endpoint.isEmpty()) {
            System.out.println("R2 storage not configured (missing R2_ACCESS_KEY / R2_ENDPOINT) - certificate uploads will fail until configured.");
            return;
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        // R2 doesn't use AWS regions, but the SDK requires one to be set - "auto" is what
        // Cloudflare recommends for R2's S3-compatible endpoint.
        s3Client = S3Client.builder()
                .endpointOverride(java.net.URI.create(endpoint))
                .credentialsProvider(credentialsProvider)
                .region(Region.of("auto"))
                .build();

        s3Presigner = S3Presigner.builder()
                .endpointOverride(java.net.URI.create(endpoint))
                .credentialsProvider(credentialsProvider)
                .region(Region.of("auto"))
                .build();
    }

    @PreDestroy
    public void cleanup() {
        if (s3Client != null) s3Client.close();
        if (s3Presigner != null) s3Presigner.close();
    }

    /**
     * Uploads a file to R2 under a generated unique key, scoped under the property's folder.
     * Returns the object key (not a URL) - store this key on the Certificate entity.
     */
    public String uploadFile(Long propertyId, MultipartFile file) throws IOException {
        String extension = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        String key = "properties/" + propertyId + "/certificates/" + UUID.randomUUID() + extension;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return key;
    }

    /**
     * Generates a time-limited download link for a private object. Valid for the given duration.
     */
    public String generatePresignedDownloadUrl(String key, Duration validFor) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(validFor)
                .getObjectRequest(getRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public void deleteFile(String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);
    }
}
