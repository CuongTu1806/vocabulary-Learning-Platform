package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.VocabularyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VocabularyRepository extends JpaRepository<VocabularyEntity, Integer> {
}
