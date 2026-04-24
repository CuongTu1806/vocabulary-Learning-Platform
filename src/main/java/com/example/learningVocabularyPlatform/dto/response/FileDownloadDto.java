package com.example.learningVocabularyPlatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class FileDownloadDto {
    private final Resource resource;
    private final String originalFilename;
    private final String contentType;
}
