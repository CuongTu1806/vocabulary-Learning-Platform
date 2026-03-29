package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.response.QuizResultDetailResponse;

import java.util.List;

public interface QuizResultService {
    List<QuizResultDetailResponse> getDetail(Long quizResultId);
}
