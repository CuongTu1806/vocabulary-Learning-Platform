package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.response.ApiResponse;

public interface ServerLeaderboardService {

    ApiResponse getGlobalLeaderboard();

    /** Hạng + điểm của một user; userId từ path hoặc sau này JWT */
    ApiResponse getUserRank(Long userId);
}
