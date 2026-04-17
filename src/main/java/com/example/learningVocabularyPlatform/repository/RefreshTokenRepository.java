package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);

    List<RefreshTokenEntity> findAllByUser_IdAndRevokedFalse(Long userId);
}
