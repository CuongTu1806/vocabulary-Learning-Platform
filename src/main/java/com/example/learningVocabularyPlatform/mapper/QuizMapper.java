package com.example.learningVocabularyPlatform.mapper;

import com.example.learningVocabularyPlatform.dto.response.QuizHistoryResponse;
import com.example.learningVocabularyPlatform.entity.QuizEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;


@Component
public class QuizMapper {
    public QuizHistoryResponse toQuizHistoryResponse(QuizEntity quiz) {
        long duration = quiz.getDuration();

        long hours = duration / 3600;
        long minutes = duration % 3600 / 60;
        long seconds = duration % 60;



        // Định dạng thành chuỗi HH:mm:ss
        String durationFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return QuizHistoryResponse.builder()
                .quizId(quiz.getId())
                .lessonName(quiz.getLesson() == null ? null : quiz.getLesson().getTitle())
                .mode(quiz.getTypeQuiz())
                .score(quiz.getScore())
                .createdAt(quiz.getCreatedAt())
                .duration(durationFormatted)
                .build();
    }
}
