package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.dto.response.LessonResponse;
import com.example.learningVocabularyPlatform.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<LessonEntity, Long> {
    List<LessonEntity> findByUser_Id(Long userId);
}
