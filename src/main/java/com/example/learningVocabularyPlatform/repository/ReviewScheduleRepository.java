package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ReviewScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewScheduleRepository extends JpaRepository<ReviewScheduleEntity, Long> {
        Optional<ReviewScheduleEntity> findTopByUser_IdAndUserVocabulary_IdOrderByUpdatedAtDesc(Long userId, Long userVocabularyId);

        boolean existsByUser_IdAndUserVocabulary_Id(Long userId, Long userVocabularyId);

        void deleteByUserVocabulary_Lesson_Id(Long lessonId);

        void deleteByUserVocabulary_Id(Long userVocabularyId);

                @Query("""
                                                SELECT rs
                                                FROM ReviewScheduleEntity rs
                                                WHERE rs.user.id = :userId
                                                        AND rs.nextReviewDate <= :now
                                                ORDER BY rs.nextReviewDate ASC
                                                """)
    List<ReviewScheduleEntity> findByUserVocabulary_User_IdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(
                                                @Param("userId") Long userId,
                                                @Param("now") LocalDateTime now
    );

                @Query("""
                                                SELECT rs
                                                FROM ReviewScheduleEntity rs
                                                WHERE rs.user.id = :userId
                                                        AND (
                                                                LOWER(COALESCE(rs.state, '')) IN ('learning', 'relearning')
                                                                OR rs.nextReviewDate <= :now
                                                        )
                                                ORDER BY
                                                        CASE WHEN LOWER(COALESCE(rs.state, '')) IN ('learning', 'relearning') THEN 0 ELSE 1 END,
                                                        rs.nextReviewDate ASC
                                                """)
                List<ReviewScheduleEntity> findActiveReviewQueueByUserId(
                                                @Param("userId") Long userId,
                                                @Param("now") LocalDateTime now
                );

                @Query("""
                                                SELECT rs
                                                FROM ReviewScheduleEntity rs
                                                WHERE rs.user.id = :userId
                                                        AND rs.nextReviewDate BETWEEN :start AND :end
                                                ORDER BY rs.nextReviewDate ASC
                                                """)
    List<ReviewScheduleEntity> findByUserVocabulary_User_IdAndNextReviewDateBetweenOrderByNextReviewDateAsc(
                                                @Param("userId") Long userId,
                                                @Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end
    );

                @Query("""
                                                SELECT COUNT(rs)
                                                FROM ReviewScheduleEntity rs
                                                WHERE rs.user.id = :userId
                                                        AND rs.nextReviewDate <= :now
                                                        AND rs.state IN :states
                                                """)
    long countByUserVocabulary_User_IdAndNextReviewDateLessThanEqualAndStateIn(
                                                @Param("userId") Long userId,
                                                @Param("now") LocalDateTime now,
                                                @Param("states") List<String> states
    );

    @Query("""
            SELECT COUNT(rs)
            FROM ReviewScheduleEntity rs
            WHERE rs.user.id = :userId
              AND LOWER(COALESCE(rs.state, '')) IN :states
            """)
    long countActiveLearningCardsByState(
            @Param("userId") Long userId,
            @Param("states") List<String> states
    );
    
    // New custom queries for profile stats
    @Query("""
            SELECT rs
            FROM ReviewScheduleEntity rs
            WHERE rs.user.id = :userId
              AND rs.lastReviewDate > :after
            """)
    List<ReviewScheduleEntity> findByUserVocabulary_User_IdAndLastReviewDateAfter(
            @Param("userId") Long userId,
            @Param("after") LocalDateTime after
    );
    
    @Query("""
            SELECT rs
            FROM ReviewScheduleEntity rs
            WHERE rs.user.id = :userId
              AND rs.lastReviewDate BETWEEN :start AND :end
            """)
    List<ReviewScheduleEntity> findByUserVocabulary_User_IdAndLastReviewDateBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    
    @Query("SELECT rs FROM ReviewScheduleEntity rs WHERE rs.user.id = :userId " +
           "AND rs.lastReviewDate IS NOT NULL " +
           "AND rs.lastReviewDate >= :startDate " +
           "AND rs.lastReviewDate < :endDate " +
           "ORDER BY rs.lastReviewDate ASC")
    List<ReviewScheduleEntity> findReviewsByDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("SELECT rs FROM ReviewScheduleEntity rs WHERE rs.user.id = :userId")
    List<ReviewScheduleEntity> findAllByUserId(@Param("userId") Long userId);
    
    @Query(value = "SELECT rs.* FROM review_schedule rs " +
            "INNER JOIN (SELECT user_vocabulary_id, MAX(id) as max_id FROM review_schedule WHERE user_id = :userId GROUP BY user_vocabulary_id) " +
           "latest ON rs.id = latest.max_id " +
            "WHERE rs.user_id = :userId", 
           nativeQuery = true)
    List<ReviewScheduleEntity> findLatestReviewForEachVocabulary(@Param("userId") Long userId);
}
