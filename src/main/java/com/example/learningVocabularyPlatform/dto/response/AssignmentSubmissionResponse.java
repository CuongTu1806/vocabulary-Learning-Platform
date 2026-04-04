package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionResponse {
    private Long id;
    private Long assignmentId;
    private Long userId;
    private String content;
    private float score;
    private LocalDateTime submittedAt;
}
