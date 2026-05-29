package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface LessonRepository extends JpaRepository<LessonEntity, Long> {
    List<LessonEntity> findByUser_Id(Long userId);

    @Query("""
                    SELECT DISTINCT l
                    FROM LessonEntity l
                    LEFT JOIN FETCH l.user u
                    WHERE LOWER(COALESCE(l.visibility, 'PRIVATE')) = 'public'
                        AND (
                            :query IS NULL OR :query = ''
                            OR LOWER(COALESCE(l.title, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                            OR LOWER(COALESCE(u.username, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                        )
                    ORDER BY COALESCE(l.downloadCount, 0) DESC, l.createdAt DESC
                    """)
    List<LessonEntity> searchPublicLessons(@Param("query") String query);

                @Query("""
                    SELECT DISTINCT l
                    FROM LessonEntity l
                    LEFT JOIN FETCH l.user u
                    WHERE LOWER(COALESCE(l.visibility, 'PRIVATE')) = 'public'
                    ORDER BY COALESCE(l.downloadCount, 0) DESC, l.createdAt DESC
                    """)
                List<LessonEntity> findAllPublicOrderByDownloadCountDesc();

                @Query("""
                    SELECT DISTINCT l
                    FROM LessonEntity l
                    LEFT JOIN FETCH l.user u
                    WHERE l.id IN :ids
                    """)
                List<LessonEntity> findAllByIdInWithOwner(@Param("ids") Collection<Long> ids);
}
