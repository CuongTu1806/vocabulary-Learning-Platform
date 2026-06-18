package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ReviewHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewHistoryRepository extends JpaRepository<ReviewHistoryEntity, Long> {
    List<ReviewHistoryEntity> findByReviewSchedule_UserVocabulary_IdOrderByCreatedAtDesc(Long userVocabularyId);

    List<ReviewHistoryEntity> findByReviewSchedule_UserVocabulary_User_IdAndCreatedAtBetween(Long userId, java.time.LocalDateTime start, java.time.LocalDateTime end);

    void deleteByReviewSchedule_UserVocabulary_Lesson_Id(Long lessonId);

    void deleteByReviewSchedule_UserVocabulary_Id(Long userVocabularyId);
}
