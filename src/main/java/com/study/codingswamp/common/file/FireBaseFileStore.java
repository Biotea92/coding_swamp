package com.study.codingswamp.common.file;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
public class FireBaseFileStore implements FileStore{

    @Value("${app.firebase-bucket}")
    private String firebaseBucket;

    @Value("${app.firebase-image-url}")
    private String imageUrlPrefix;

    private static final String imageUrlSuffix = "?alt=media";
    private static final String defaultImageUrl = "https://firebasestorage.googleapis.com/v0/b/coding-swamp.appspot.com/o/default_image%2Fcrocodile.png?alt=media";

    @Override
    public String storeFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return defaultImageUrl;
        }
        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);
        String imageUrl = upload(multipartFile, storeFileName);
        return imageUrl;
    }

    @Override
    public void deleteFile(String imageUrl) {
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
