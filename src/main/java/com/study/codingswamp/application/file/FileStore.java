package com.study.codingswamp.application.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStore {

    String storeFile(MultipartFile multipartFile);

    void deleteFile(String imageUrl);
}
