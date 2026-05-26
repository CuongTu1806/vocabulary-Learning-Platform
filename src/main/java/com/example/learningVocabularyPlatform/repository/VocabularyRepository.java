package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.VocabularyEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VocabularyRepository extends JpaRepository<VocabularyEntity, Long> {
    List<VocabularyEntity> findByWordContaining(String word);

    List<VocabularyEntity> findByPosIgnoreCase(String pos);

    @Query("SELECT v FROM VocabularyEntity v WHERE " +
           "LOWER(COALESCE(v.word, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(v.meaning, '')) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<VocabularyEntity> searchByWordOrMeaningContaining(@Param("q") String q, Pageable pageable);
}
