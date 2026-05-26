package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.UserVocabularyEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserVocabularyRepository extends JpaRepository<UserVocabularyEntity, Long> {
    List<UserVocabularyEntity> findByLesson_Id(Long lessonId);

    List<UserVocabularyEntity> findByWordContaining(String word);

    @Query("SELECT uv FROM UserVocabularyEntity uv WHERE " +
           "LOWER(COALESCE(uv.word, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(uv.meaning, '')) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<UserVocabularyEntity> searchByWordOrMeaningContaining(@Param("q") String q, Pageable pageable);

    List<UserVocabularyEntity> findByUser_Id(Long userId);

    List<UserVocabularyEntity> findByUser_IdAndStatus(Long userId, String status);

    java.util.Optional<UserVocabularyEntity> findByIdAndUser_Id(Long id, Long userId);

    int countByLesson_Id(Long id);
    
    // New custom queries for profile stats
    @Query("SELECT uv FROM UserVocabularyEntity uv WHERE uv.user.id = :userId " +
           "AND uv.createdAt >= :startDate AND uv.createdAt < :endDate " +
           "ORDER BY uv.createdAt ASC")
    List<UserVocabularyEntity> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
