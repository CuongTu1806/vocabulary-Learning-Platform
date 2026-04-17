package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ReviewSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewSettingRepository extends JpaRepository<ReviewSettingEntity, Long> {
    Optional<ReviewSettingEntity> findByUser_Id(Long userId);
}
