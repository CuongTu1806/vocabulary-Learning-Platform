package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.LessonAccessEntity;
import com.example.learningVocabularyPlatform.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LessonAccessRepository extends JpaRepository<LessonAccessEntity, Long> {
    boolean existsByUser_IdAndLesson_Id(Long userId, Long lessonId);

    void deleteByLesson_Id(Long lessonId);

    @Query("""
            SELECT DISTINCT l
            FROM LessonAccessEntity la
            JOIN la.lesson l
            LEFT JOIN FETCH l.user u
            WHERE la.user.id = :userId
            ORDER BY l.createdAt DESC
            """)
    List<LessonEntity> findAccessibleLessonsByUserId(@Param("userId") Long userId);

        @Query("""
            SELECT la.lesson.id AS lessonId, COUNT(la) AS downloadCount
            FROM LessonAccessEntity la
            WHERE la.createdAt BETWEEN :start AND :end
              AND LOWER(COALESCE(la.lesson.visibility, '')) = 'public'
            GROUP BY la.lesson.id
            """)
        List<LessonAccessAggregateRow> aggregateAccessBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
        );
}