package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.LessonDownloadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LessonDownloadRepository extends JpaRepository<LessonDownloadEntity, Long> {

    @Query("""
            SELECT ld.lesson.id AS lessonId, COUNT(ld) AS downloadCount
            FROM LessonDownloadEntity ld
            WHERE ld.createdAt BETWEEN :start AND :end
              AND LOWER(COALESCE(ld.lesson.visibility, '')) = 'public'
            GROUP BY ld.lesson.id
            """)
    List<LessonDownloadAggregateRow> aggregateDownloadsBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}