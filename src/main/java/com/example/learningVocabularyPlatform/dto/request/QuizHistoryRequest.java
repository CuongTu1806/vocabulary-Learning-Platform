package com.example.learningVocabularyPlatform.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class QuizHistoryRequest {
    private String name;
    private String mode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long minScore; // >=
    private Long maxScore; // <=
}
