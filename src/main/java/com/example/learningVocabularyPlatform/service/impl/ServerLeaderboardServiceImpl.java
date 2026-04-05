package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.LeaderboardResponse;
import com.example.learningVocabularyPlatform.entity.UserEntity;
import com.example.learningVocabularyPlatform.repository.ContestSubmissionRepository;
import com.example.learningVocabularyPlatform.repository.UserContestAggregateRow;
import com.example.learningVocabularyPlatform.repository.UserRepository;
import com.example.learningVocabularyPlatform.service.ServerLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerLeaderboardServiceImpl implements ServerLeaderboardService {

    private final ContestSubmissionRepository contestSubmissionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getGlobalLeaderboard() {
        List<LeaderboardResponse> list = computeRankedLeaderboard();
        if (list.isEmpty()) {
            return ApiResponse.builder()
                    .message("Chưa có dữ liệu contest submission")
                    .data(List.of())
                    .build();
        }
        return ApiResponse.builder().message("OK").data(list).build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getUserRank(Long userId) {
        Optional<LeaderboardResponse> row = computeRankedLeaderboard().stream()
                .filter(r -> userId.equals(r.getUserId()))
                .findFirst();

        if (row.isPresent()) {
            return ApiResponse.builder().message("OK").data(row.get()).build();
        }

        UserEntity u = userRepository.findById(userId).orElse(null);
        return ApiResponse.builder()
                .message("User chưa có điểm contest (chưa nộp bài)")
                .data(LeaderboardResponse.builder()
                        .rank(null)
                        .userId(userId)
                        .username(u != null ? u.getUsername() : null)
                        .rating(0)
                        .contestCount(0)
                        .build())
                .build();
    }

    /**
     * Aggregate từ contest_submission → sort → gán rank kiểu thi đấu (1,1,3) + username.
     */
    private List<LeaderboardResponse> computeRankedLeaderboard() {
        List<UserContestAggregateRow> rows = contestSubmissionRepository.aggregateScoresByUser();
        if (rows.isEmpty()) {
            return List.of();
        }

        List<UserContestAggregateRow> sorted = rows.stream()
                .sorted(Comparator
                        .comparing(UserContestAggregateRow::getTotalScore, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()
                        .thenComparing(UserContestAggregateRow::getUserId, Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();

        Map<Long, UserEntity> users = userRepository.findAllById(
                sorted.stream().map(UserContestAggregateRow::getUserId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        List<LeaderboardResponse> list = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            UserContestAggregateRow row = sorted.get(i);
            if (i > 0 && !Objects.equals(row.getTotalScore(), sorted.get(i - 1).getTotalScore())) {
                rank = i + 1;
            }
            UserEntity u = users.get(row.getUserId());
            list.add(LeaderboardResponse.builder()
                    .rank(rank)
                    .userId(row.getUserId())
                    .username(u != null ? u.getUsername() : null)
                    .rating(row.getTotalScore() != null ? row.getTotalScore().intValue() : 0)
                    .contestCount(row.getContestCount() != null ? row.getContestCount().intValue() : 0)
                    .build());
        }
        return list;
    }
}
