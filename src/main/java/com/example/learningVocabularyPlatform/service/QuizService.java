package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.request.QuizRequest;
import com.example.learningVocabularyPlatform.dto.response.QuizResponse;

import java.util.List;

public interface QuizService {
    List<QuizResponse> getQuiz(long lessonId, long userId, QuizRequest request);
}
