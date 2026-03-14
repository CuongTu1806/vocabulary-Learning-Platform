package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
