package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.VocabularyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VocabularyRepository extends JpaRepository<VocabularyEntity, Long> {
    List<VocabularyEntity> findByWord(String word);
}
