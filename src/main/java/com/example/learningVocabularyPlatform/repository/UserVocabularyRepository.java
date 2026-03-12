package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.UserVocabularyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVocabularyRepository extends JpaRepository<UserVocabularyEntity, Integer> {
    List<UserVocabularyEntity> findByLessonId(Long lesson_id);
}
