package com.example.learningVocabularyPlatform.repository;

import com.example.learningVocabularyPlatform.entity.ContestSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ContestSubmissionRepository extends JpaRepository<ContestSubmissionEntity, Long> {

    List<ContestSubmissionEntity> findByContest_Id(Long contestId);

    Optional<ContestSubmissionEntity> findByContest_IdAndProblem_IdAndUser_Id(
            Long contestId, Long problemId, Long userId);

    /**
     * Nguồn sự thật cho leaderboard global: rating = SUM(score), contestCount = số contest khác nhau đã nộp bài.
     */
    @Query("""
            SELECT s.user.id AS userId, SUM(s.score) AS totalScore, COUNT(DISTINCT s.contest.id) AS contestCount
            FROM ContestSubmissionEntity s
            GROUP BY s.user.id
            """)
    List<UserContestAggregateRow> aggregateScoresByUser();
}
