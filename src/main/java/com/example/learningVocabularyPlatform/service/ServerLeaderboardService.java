package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.response.ApiResponse;

public interface ServerLeaderboardService {

    ApiResponse getGlobalLeaderboard();

    ApiResponse getUserRank(Long userId);

    ApiResponse getContestRankings(String mode);

    ApiResponse getLessonRankings(String range);
}
