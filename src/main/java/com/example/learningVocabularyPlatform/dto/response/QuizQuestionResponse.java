package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class QuizQuestionResponse {
    private Long quizResultId;
    private String content;
    private List<String> answers;
}
