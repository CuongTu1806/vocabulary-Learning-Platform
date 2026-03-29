package com.example.learningVocabularyPlatform.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AnswerSubmitRequest {
    private Long questionId; // quizResultId
    private String userAnswer;
}
