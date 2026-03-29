package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.request.QuizHistoryRequest;
import com.example.learningVocabularyPlatform.dto.request.QuizRequest;
import com.example.learningVocabularyPlatform.dto.request.QuizSubmitRequest;
import com.example.learningVocabularyPlatform.dto.response.QuizHistoryResponse;
import com.example.learningVocabularyPlatform.dto.response.QuizResponse;
import com.example.learningVocabularyPlatform.dto.response.QuizSubmitResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizService {
    QuizResponse getQuiz(long lessonId, long userId, QuizRequest request);
    QuizSubmitResponse submitQuiz(long userId, QuizSubmitRequest request);
    Page<QuizHistoryResponse> getQuizHistory(Long userId, QuizHistoryRequest request, Pageable pageable);
}
