package com.study.codingswamp.application.file;

import com.study.codingswamp.application.file.FileStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FileStoreTest {

    @Autowired
    FileStore fileStore;

    @Value ("${app.firebase-image-url-prefix}")
    private String imageUrlPrefix;

    @Value("${app.firebase-image-url-suffix}")
    private String imageUrlSuffix;

    @Test
    @DisplayName("multipartFile은 업로드 되고 imageUrl을 반환한다. 마지막에는 삭제한다")
    void uploadFile() {
        // given
        MockMultipartFile mockMultipartFile = new MockMultipartFile("MockFile.png", "MockFile.png".getBytes());

        // when
        String imageUrl = fileStore.storeFile(mockMultipartFile);

        // then
        assertThat(imageUrl).startsWith(imageUrlPrefix);
        assertThat(imageUrl).endsWith(imageUrlSuffix);

        fileStore.deleteFile(imageUrl);
    }
}