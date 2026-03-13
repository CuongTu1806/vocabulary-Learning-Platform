package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.UserVocabularyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserVocabularyRepository extends JpaRepository<UserVocabularyEntity, Long> {
    List<UserVocabularyEntity> findByLesson_Id(Long lessonId);

    List<UserVocabularyEntity> findByWord(String word);
}
