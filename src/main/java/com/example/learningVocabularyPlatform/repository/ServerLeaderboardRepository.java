package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ServerLeaderboardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServerLeaderboardRepository extends JpaRepository<ServerLeaderboardEntity, Long> {

    /** Dùng sau này nếu cần đồng bộ cache / rebuild từ aggregate */
    Optional<ServerLeaderboardEntity> findByUser_Id(Long userId);
}
