package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class QuizResponse {
    private String quizType;
    private Long lessonId;
    private Long userId;
    private String question;
    private List<String> distractor;
    private String correctAnswer;
}
