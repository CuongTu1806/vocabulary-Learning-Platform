package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.request.LessonRequest;
import com.example.learningVocabularyPlatform.dto.request.VocabularyAddRequest;
import com.example.learningVocabularyPlatform.dto.response.LessonResponse;
import com.example.learningVocabularyPlatform.dto.response.UserVocabularyResponse;

import java.util.List;

public interface LessonService {
    List<LessonResponse> getAll(Long userId);
    LessonResponse createLesson(Long userId, LessonRequest lessonRequest);
    UserVocabularyResponse addVocab(Long lessonId, VocabularyAddRequest request, Long userId);
    LessonResponse updateLesson(Long userId, Long lessonId, LessonRequest request);
    void deleteLesson(Long userId, Long lessonId);
}
