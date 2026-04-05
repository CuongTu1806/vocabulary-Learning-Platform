package com.example.learningVocabularyPlatform.repository;

/**
 * Kết quả GROUP BY user từ {@link com.example.learningVocabularyPlatform.entity.ContestSubmissionEntity}:
 * tổng điểm mọi contest + số contest đã tham gia (có ít nhất một submission).
 */
public interface UserContestAggregateRow {

    Long getUserId();

    Long getTotalScore();

    Long getContestCount();
}
