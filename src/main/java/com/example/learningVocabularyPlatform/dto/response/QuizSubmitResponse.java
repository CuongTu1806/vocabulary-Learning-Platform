package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class QuizSubmitResponse {
    private Long quizId;
    private int totalQuestions;
    private int correctAnswers;
    private Double correctPercentage;

    private List<QuizResultDetailResponse> quizResultDetailResponse;
}
