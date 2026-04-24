package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentAttachmentResponse {
    private Long id;
    private String originalFilename;
    private String contentType;
    private Long sizeBytes;
}
