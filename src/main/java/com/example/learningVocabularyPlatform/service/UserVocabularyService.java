package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.request.VocabularyAddRequest;
import com.example.learningVocabularyPlatform.dto.response.UserVocabularyResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

public interface UserVocabularyService {
    List<UserVocabularyResponse> searchVocabulary(String keyword);

    UserVocabularyResponse updateVocabInLesson(Long vocabId, VocabularyAddRequest request);

    void deleteVocabInLesson(Long vocabId);
}
