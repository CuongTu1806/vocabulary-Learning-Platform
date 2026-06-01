package com.example.learningVocabularyPlatform.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Lưu file dưới thư mục con (vd assignment/12, submission/3). Trả về đường dẫn tương đối so với root.
     */
    String store(MultipartFile file, String subdirectory) throws java.io.IOException;

    void deleteIfExists(String relativePath);

    Resource loadAsResource(String relativePath);

    boolean exists(String relativePath);
}
