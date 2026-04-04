package com.example.learningVocabularyPlatform.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionRequest {
    private String content;
}
