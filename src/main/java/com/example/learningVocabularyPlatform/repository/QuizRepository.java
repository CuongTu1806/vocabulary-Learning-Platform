package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.QuizEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuizRepository extends JpaRepository<QuizEntity, Long>, JpaSpecificationExecutor<QuizEntity> {
}
