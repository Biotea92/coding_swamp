package com.study.codingswamp.application.file;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.study.codingswamp.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
public class FireBaseFileStore implements FileStore{

    private final String firebaseBucket;
    private final String imageUrlPrefix;
    private final String imageUrlSuffix;
    private final String defaultImageUrl;

    public FireBaseFileStore(
            @Value("${app.firebase-bucket}") String firebaseBucket,
            @Value("${app.firebase-image-url-prefix}") String imageUrlPrefix,
            @Value("${app.firebase-image-url-suffix}") String imageUrlSuffix,
            @Value("${app.firebase-default-image-url}") String defaultImageUrl) {
        this.firebaseBucket = firebaseBucket;
        this.imageUrlPrefix = imageUrlPrefix;
        this.imageUrlSuffix = imageUrlSuffix;
        this.defaultImageUrl = defaultImageUrl;
    }

    @Override
    public String storeFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return defaultImageUrl;
        }
        validateContentTypeImage(multipartFile);

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);
        return upload(multipartFile, storeFileName);
    }

    private static void validateContentTypeImage(MultipartFile multipartFile) {
        String contentType = multipartFile.getContentType();
        if (contentType != null && !contentType.startsWith("image")) {
            throw new InvalidRequestException("imageFile", "image 파일이 아닙니다.");
        }
    }

    @Override
    public void deleteFile(String imageUrl) {
        if (imageUrl.equals(defaultImageUrl)) {
            return;
        }
        String fileName = deleteFileNameExtract(imageUrl);
        Bucket bucket = StorageClient.getInstance().bucket(firebaseBucket);
        Blob blob = bucket.get(fileName);
        blob.delete();
    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    private String upload(MultipartFile multipartFile, String storeFileName) {
        Bucket bucket = StorageClient.getInstance().bucket(firebaseBucket);
        try {
            InputStream content = new ByteArrayInputStream(multipartFile.getBytes());
            bucket.create(storeFileName, content, multipartFile.getContentType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return imageUrlPrefix + storeFileName + imageUrlSuffix;
    }

    private String deleteFileNameExtract(String imageUrl) {
        int indexOfSuffix = imageUrl.indexOf(imageUrlSuffix);
        return imageUrl.substring(imageUrlPrefix.length(), indexOfSuffix);
    }
}
