package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ReviewScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewScheduleRepository extends JpaRepository<ReviewScheduleEntity, Long> {
    Optional<ReviewScheduleEntity> findTopByUserVocabulary_IdOrderByUpdatedAtDesc(Long userVocabularyId);

    List<ReviewScheduleEntity> findByUserVocabulary_User_IdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(
	    Long userId,
	    LocalDateTime now
    );

    List<ReviewScheduleEntity> findByUserVocabulary_User_IdAndNextReviewDateBetweenOrderByNextReviewDateAsc(
	    Long userId,
	    LocalDateTime start,
	    LocalDateTime end
    );

    long countByUserVocabulary_User_IdAndNextReviewDateLessThanEqualAndStateIn(
	    Long userId,
	    LocalDateTime now,
	    List<String> states
    );
    
    // New custom queries for profile stats
    List<ReviewScheduleEntity> findByUserVocabulary_User_IdAndLastReviewDateAfter(
            Long userId,
            LocalDateTime after
    );
    
    List<ReviewScheduleEntity> findByUserVocabulary_User_IdAndLastReviewDateBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
    
    @Query("SELECT rs FROM ReviewScheduleEntity rs WHERE rs.userVocabulary.user.id = :userId " +
           "AND rs.lastReviewDate IS NOT NULL " +
           "AND rs.lastReviewDate >= :startDate " +
           "AND rs.lastReviewDate < :endDate " +
           "ORDER BY rs.lastReviewDate ASC")
    List<ReviewScheduleEntity> findReviewsByDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("SELECT rs FROM ReviewScheduleEntity rs WHERE rs.userVocabulary.user.id = :userId")
    List<ReviewScheduleEntity> findAllByUserId(@Param("userId") Long userId);
    
    @Query(value = "SELECT rs.* FROM review_schedule rs " +
           "INNER JOIN (SELECT user_vocabulary_id, MAX(id) as max_id FROM review_schedule GROUP BY user_vocabulary_id) " +
           "latest ON rs.id = latest.max_id " +
           "WHERE rs.user_vocabulary_id IN (SELECT id FROM user_vocabulary WHERE user_id = :userId)", 
           nativeQuery = true)
    List<ReviewScheduleEntity> findLatestReviewForEachVocabulary(@Param("userId") Long userId);
}
