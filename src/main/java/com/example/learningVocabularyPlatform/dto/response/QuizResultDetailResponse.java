package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class QuizResultDetailResponse {
    private Long quizResultId;
    private String content;
    private String userAnswer;
    private String correctAnswer;
    private boolean correct;
}
