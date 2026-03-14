package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.response.UserVocabularyResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

public interface UserVocabularyService {
    List<UserVocabularyResponse> getVocabInLesson(Long lesson_id);

    List<UserVocabularyResponse> searchVocabulary(String keyword);
}
