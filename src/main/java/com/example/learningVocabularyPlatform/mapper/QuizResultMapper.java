package com.example.learningVocabularyPlatform.mapper;

import com.example.learningVocabularyPlatform.dto.response.QuizResultDetailResponse;
import com.example.learningVocabularyPlatform.entity.QuizResultEntity;
import org.springframework.stereotype.Component;

@Component
public class QuizResultMapper {
    public QuizResultDetailResponse entityToResponse(QuizResultEntity quizResultEntity) {
        return QuizResultDetailResponse.builder()
                .quizResultId(quizResultEntity.getId())
                .userAnswer(quizResultEntity.getUserAnswer())
                .content(quizResultEntity.getContent())
                .correctAnswer(quizResultEntity.getTrueAnswer())
                .correct(quizResultEntity.isCorrect())
                .build();
    }
}
