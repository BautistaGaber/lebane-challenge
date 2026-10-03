package com.lebane.backend.storage;

import com.lebane.backend.common.exception.InvalidFileException;
import com.lebane.backend.common.exception.StorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

@Service
public class S3StorageService implements StorageService{

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Duration URL_EXPIRATION = Duration.ofHours(1);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties properties;

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner, StorageProperties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    @Override
    public StoredObject upload(MultipartFile file, String objectKey) {

        validateFile(file);

        String contentType = file.getContentType();

        PutObjectRequest request = PutObjectRequest.builder().bucket(properties.bucket()).key(objectKey).contentType(contentType).build();

        try {
            s3Client.putObject(request,RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return new StoredObject(objectKey, contentType);

        } catch (IOException exception) {
            throw new StorageException("Could not read image file", exception);

        } catch (RuntimeException exception) {
            throw new StorageException("Could not upload image to storage", exception);
        }
    }

    @Override
    public StoredObject upload(byte[] content, String contentType, String objectKey) {
        if(content == null || content.length == 0){
            throw new InvalidFileException("Image content cannot be empty");
        }
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(request,RequestBody.fromBytes(content));

            return new StoredObject(objectKey,contentType);

        } catch (RuntimeException exception) {
            throw new StorageException("Could not upload image to storage",exception);
        }
    }

    @Override
    public void delete(String objectKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(properties.bucket()).key(objectKey).build();
        try {
            s3Client.deleteObject(request);
        } catch (RuntimeException exception) {
            throw new StorageException("Could not delete image from storage", exception);
        }
    }

    @Override
    public String getUrl(String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(URL_EXPIRATION)
                        .getObjectRequest(getObjectRequest)
                        .build();

        try {
            return s3Presigner.presignGetObject(presignRequest).url().toString();

        } catch (RuntimeException exception) {
            throw new StorageException("Could not generate image URL", exception);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(objectKey)
                    .build();

            s3Client.headObject(request);

            return true;

        } catch (NoSuchKeyException exception) {
            return false;

        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                return false;
            }

            throw new StorageException("Could not verify object existence in storage", exception);
        }
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Image file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("Image file exceeds the maximum allowed size");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {

            throw new InvalidFileException("Unsupported image format");
        }
    }
}
