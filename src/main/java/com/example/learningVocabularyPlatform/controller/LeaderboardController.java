package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.service.ServerLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Leaderboard global: rating = tổng điểm từ mọi bản ghi contest_submission.
 * TODO: sau khi có JWT, {@code /me} lấy userId từ token thay vì hằng số.
 */
@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private static final Long HARDCODE_ME_USER_ID = 4L;

    private final ServerLeaderboardService serverLeaderboardService;

    @GetMapping("/global")
    public ResponseEntity<ApiResponse> global() {
        return ResponseEntity.ok(serverLeaderboardService.getGlobalLeaderboard());
    }

    @GetMapping("/contest")
    public ResponseEntity<ApiResponse> contest(@RequestParam(defaultValue = "all_time") String mode) {
        return ResponseEntity.ok(serverLeaderboardService.getContestRankings(mode));
    }

    @GetMapping("/lessons")
    public ResponseEntity<ApiResponse> lessons(@RequestParam(defaultValue = "all_time") String range) {
        return ResponseEntity.ok(serverLeaderboardService.getLessonRankings(range));
    }

    @GetMapping("/users/{userId}/rank")
    public ResponseEntity<ApiResponse> userRank(@PathVariable Long userId) {
        return ResponseEntity.ok(serverLeaderboardService.getUserRank(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> myRank() {
        return ResponseEntity.ok(serverLeaderboardService.getUserRank(HARDCODE_ME_USER_ID));
    }
}
