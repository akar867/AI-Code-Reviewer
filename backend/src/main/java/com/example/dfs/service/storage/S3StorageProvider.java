package com.example.dfs.service.storage;

import com.example.dfs.config.StorageProperties;
import com.example.dfs.model.StorageNode;
import com.example.dfs.service.StoredFileReference;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
public class S3StorageProvider implements StorageProvider {

    private final StorageProperties.S3Properties properties;
    private final S3Client s3Client;

    public S3StorageProvider(StorageProperties storageProperties) {
        this.properties = storageProperties.getS3();
        if (properties.getBucket() == null || properties.getAccessKey() == null || properties.getSecretKey() == null) {
            throw new IllegalStateException("S3 bucket and credentials must be set when mode=S3");
        }
        AwsBasicCredentials creds = AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey());
        S3ClientBuilder builder = S3Client.builder()
                .region(software.amazon.awssdk.regions.Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(creds));
        if (properties.getEndpoint() != null) {
            builder = builder.endpointOverride(java.net.URI.create(properties.getEndpoint()));
        }
        this.s3Client = builder.build();
        ensureBucket();
    }

    @Override
    public StoredFileReference store(StorageNode node, String relativePath, InputStream inputStream) throws IOException {
        byte[] data = inputStream.readAllBytes();
        String key = node.getId() + "/" + relativePath;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromBytes(data));
            return new StoredFileReference(node.getId(), relativePath);
        } catch (Exception ex) {
            throw new IOException("Failed to write to S3", ex);
        }
    }

    @Override
    public Resource load(StorageNode node, String relativePath) throws IOException {
        String key = node.getId() + "/" + relativePath;
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .build();
        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
            return new ByteArrayResource(response.readAllBytes());
        } catch (Exception ex) {
            throw new IOException("Failed to read from S3", ex);
        }
    }

    @Override
    public void delete(StorageNode node, String relativePath) throws IOException {
        String key = node.getId() + "/" + relativePath;
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .build();
        try {
            s3Client.deleteObject(request);
        } catch (Exception ex) {
            log.warn("Failed to delete {} from S3", key, ex);
        }
    }

    private void ensureBucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(properties.getBucket()).build());
        } catch (NoSuchBucketException ex) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(properties.getBucket()).build());
        } catch (Exception ex) {
            log.warn("Bucket validation failed: {}", ex.getMessage());
        }
    }
}
