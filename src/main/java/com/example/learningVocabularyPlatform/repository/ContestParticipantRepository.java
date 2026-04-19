package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ContestParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContestParticipantRepository extends JpaRepository<ContestParticipantEntity, Long> {

    boolean existsByContest_IdAndUser_Id(Long contestId, Long userId);

    Optional<ContestParticipantEntity> findByContest_IdAndUser_Id(Long contestId, Long userId);

    List<ContestParticipantEntity> findByContest_Id(Long contestId);
}
