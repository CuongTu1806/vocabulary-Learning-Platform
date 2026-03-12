package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.entity.UserVocabularyEntity;
import com.example.learningVocabularyPlatform.repository.UserVocabularyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserVocabularyService {
    private static UserVocabularyRepository userVocabularyRepository;

    public static List<UserVocabularyEntity> getVocabularyByLesson(Long lessonId) {
        return userVocabularyRepository.findByLessonId(lessonId);
    }
}
