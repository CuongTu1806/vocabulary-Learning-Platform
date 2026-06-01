package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ContestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestRepository extends JpaRepository<ContestEntity, Long> {
}
