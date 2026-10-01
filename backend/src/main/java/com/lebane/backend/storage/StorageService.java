package com.lebane.backend.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StoredObject upload(MultipartFile file, String objectKey);

    StoredObject upload(byte[] content, String contentType, String objectKey);

    void delete(String objectKey);

    String getUrl(String objectKey);
}
