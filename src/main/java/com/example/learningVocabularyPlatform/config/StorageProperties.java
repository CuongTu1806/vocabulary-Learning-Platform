package com.example.learningVocabularyPlatform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /** Thư mục gốc lưu file (relative hoặc absolute). */
    private String root = "./uploads";

    /** Kích thước tối đa mỗi file (bytes), mặc định 10MB. */
    private long maxFileSizeBytes = 10L * 1024 * 1024;

    /** Giới hạn số file mỗi lần upload (đề hoặc nộp bài). */
    private int maxFilesPerRequest = 20;
}
