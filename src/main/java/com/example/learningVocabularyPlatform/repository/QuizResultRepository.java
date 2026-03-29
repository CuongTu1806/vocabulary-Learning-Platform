package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.QuizResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResultEntity, Long> {
    List<QuizResultEntity> findByQuiz_id(Long quizId);

    List<QuizResultEntity> findByQuiz_Id(Long quizId);
}
