package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.UserVocabularyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserVocabularyRepository extends JpaRepository<UserVocabularyEntity, Long> {
    List<UserVocabularyEntity> findByLesson_Id(Long lessonId);

    List<UserVocabularyEntity> findByWordContaining(String word);

    List<UserVocabularyEntity> findByUser_Id(Long userId);

    List<UserVocabularyEntity> findByUser_IdAndStatus(Long userId, String status);

    java.util.Optional<UserVocabularyEntity> findByIdAndUser_Id(Long id, Long userId);

    int countByLesson_Id(Long id);
}
