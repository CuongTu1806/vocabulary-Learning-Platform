package com.example.learningVocabularyPlatform.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class QuizSubmitRequest {

    private Long quizId;
    private List<AnswerSubmitRequest> answers;
    private Long durationInSeconds; // Thời gian làm quiz tính từ frontend (giây)
}
