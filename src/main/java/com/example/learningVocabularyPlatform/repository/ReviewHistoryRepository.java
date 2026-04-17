package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ReviewHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewHistoryRepository extends JpaRepository<ReviewHistoryEntity, Long> {
    List<ReviewHistoryEntity> findByUserVocabulary_IdOrderByCreatedAtDesc(Long userVocabularyId);
}
