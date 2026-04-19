package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.service.ServerLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/users/{userId}/rank")
    public ResponseEntity<ApiResponse> userRank(@PathVariable Long userId) {
        return ResponseEntity.ok(serverLeaderboardService.getUserRank(userId));
    }

    /** Tiện test cùng user với contest submit (HARDCODE_STUDENT_ID = 4) */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> myRank() {
        return ResponseEntity.ok(serverLeaderboardService.getUserRank(HARDCODE_ME_USER_ID));
    }
}
