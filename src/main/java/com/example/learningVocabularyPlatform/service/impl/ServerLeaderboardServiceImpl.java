package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.LeaderboardResponse;
import com.example.learningVocabularyPlatform.dto.response.LessonLeaderboardResponse;
import com.example.learningVocabularyPlatform.entity.ContestSubmissionEntity;
import com.example.learningVocabularyPlatform.entity.LessonEntity;
import com.example.learningVocabularyPlatform.entity.UserEntity;
import com.example.learningVocabularyPlatform.repository.ContestSubmissionRepository;
import com.example.learningVocabularyPlatform.repository.LessonAccessAggregateRow;
import com.example.learningVocabularyPlatform.repository.LessonAccessRepository;
import com.example.learningVocabularyPlatform.repository.LessonRepository;
import com.example.learningVocabularyPlatform.repository.UserRepository;
import com.example.learningVocabularyPlatform.service.ServerLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerLeaderboardServiceImpl implements ServerLeaderboardService {

    private static final String MODE_ALL_TIME = "all_time";
    private static final String MODE_LATEST_CONTEST = "latest_contest";
    private static final String MODE_CONTEST_COUNT = "contest_count";

    private static final String RANGE_WEEK = "week";
    private static final String RANGE_MONTH = "month";

    private final ContestSubmissionRepository contestSubmissionRepository;
    private final LessonAccessRepository lessonAccessRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getGlobalLeaderboard() {
        return getContestRankings(MODE_ALL_TIME);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getUserRank(Long userId) {
        Optional<LeaderboardResponse> row = computeContestLeaderboard(MODE_ALL_TIME).stream()
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

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getContestRankings(String mode) {
        List<LeaderboardResponse> list = computeContestLeaderboard(mode);
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
    public ApiResponse getLessonRankings(String range) {
        List<LessonLeaderboardResponse> list = computeLessonLeaderboard(range);
        if (list.isEmpty()) {
            return ApiResponse.builder()
                    .message("Chưa có dữ liệu tải lesson")
                    .data(List.of())
                    .build();
        }
        return ApiResponse.builder().message("OK").data(list).build();
    }

    private List<LeaderboardResponse> computeContestLeaderboard(String mode) {
        ContestLeaderboardMode leaderboardMode = ContestLeaderboardMode.from(mode);
        List<ContestSubmissionEntity> submissions = contestSubmissionRepository.findAllDetailed();
        if (submissions.isEmpty()) {
            return List.of();
        }

        Map<Long, UserContestStats> statsByUser = new HashMap<>();
        // determine latest contest across all submissions (server-global)
        Long globalLatestContestId = submissions.stream()
            .filter(s -> s.getContest() != null && s.getSubmittedAt() != null)
            .max(Comparator.comparing(ContestSubmissionEntity::getSubmittedAt))
            .map(s -> s.getContest().getId())
            .orElse(null);
        for (ContestSubmissionEntity submission : submissions) {
            if (submission.getUser() == null || submission.getContest() == null) {
                continue;
            }
            Long userId = submission.getUser().getId();
            Long contestId = submission.getContest().getId();
            UserContestStats stats = statsByUser.computeIfAbsent(userId, ignored -> new UserContestStats());
            stats.addSubmission(contestId, submission.getScore(), submission.getSubmittedAt());
        }

        if (statsByUser.isEmpty()) {
            return List.of();
        }

        Set<Long> userIds = statsByUser.keySet();
        Map<Long, UserEntity> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user));

        List<ContestLeaderboardRow> rows = statsByUser.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    UserContestStats stats = entry.getValue();
                    Long metric;
                    if (leaderboardMode == ContestLeaderboardMode.LATEST_CONTEST) {
                        if (globalLatestContestId == null) {
                            metric = 0L;
                        } else {
                            metric = stats.scoreForContest(globalLatestContestId);
                        }
                    } else {
                        metric = leaderboardMode.metric(stats);
                    }
                    return new ContestLeaderboardRow(
                            userId,
                            users.get(userId) != null ? users.get(userId).getUsername() : null,
                            stats.allTimeScore(),
                            stats.contestCount(),
                            stats.latestContestScore(),
                            metric
                    );
                })
                .sorted(Comparator
                        .comparing(ContestLeaderboardRow::metric, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()
                        .thenComparing(ContestLeaderboardRow::userId, Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();

        return buildContestResponses(rows);
    }

    private List<LessonLeaderboardResponse> computeLessonLeaderboard(String range) {
        LessonLeaderboardRange leaderboardRange = LessonLeaderboardRange.from(range);

        if (leaderboardRange == LessonLeaderboardRange.ALL_TIME) {
            List<LessonEntity> lessons = lessonRepository.findAllPublicOrderByDownloadCountDesc();
            if (lessons.isEmpty()) {
                return List.of();
            }

            List<LessonMetricRow> rows = lessons.stream()
                    .map(lesson -> new LessonMetricRow(
                            lesson.getId(),
                            lesson.getTitle(),
                            lesson.getUser() != null ? lesson.getUser().getId() : null,
                            lesson.getUser() != null ? lesson.getUser().getUsername() : null,
                            lesson.getDownloadCount() != null ? lesson.getDownloadCount().longValue() : 0L
                    ))
                    .sorted(Comparator
                            .comparing(LessonMetricRow::metric, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()
                            .thenComparing(LessonMetricRow::lessonId, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .toList();
            return buildLessonResponses(rows);
        }

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;
        // change: interpret WEEK as last 7 days and MONTH as last 30 days (recent window)
        if (leaderboardRange == LessonLeaderboardRange.WEEK) {
            start = LocalDate.now().minusDays(6).atStartOfDay(); // 7-day window including today
        } else {
            start = LocalDate.now().minusDays(29).atStartOfDay(); // 30-day window including today
        }

        List<LessonAccessAggregateRow> aggregates = lessonAccessRepository.aggregateAccessBetween(start, end);
        if (aggregates.isEmpty()) {
            // No recent download logs found — fall back to all-time download counts so UI shows useful data
            List<LessonEntity> lessons = lessonRepository.findAllPublicOrderByDownloadCountDesc();
            if (lessons == null || lessons.isEmpty()) {
            return List.of();
            }

            List<LessonMetricRow> rows = lessons.stream()
                .map(lesson -> new LessonMetricRow(
                    lesson.getId(),
                    lesson.getTitle(),
                    lesson.getUser() != null ? lesson.getUser().getId() : null,
                    lesson.getUser() != null ? lesson.getUser().getUsername() : null,
                    lesson.getDownloadCount() != null ? lesson.getDownloadCount().longValue() : 0L
                ))
                .sorted(Comparator
                    .comparing(LessonMetricRow::metric, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()
                    .thenComparing(LessonMetricRow::lessonId, Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();

            return buildLessonResponses(rows);
        }

        Map<Long, Long> countsByLessonId = aggregates.stream()
            .collect(Collectors.toMap(LessonAccessAggregateRow::getLessonId, LessonAccessAggregateRow::getDownloadCount));

        Map<Long, LessonEntity> lessonsById = lessonRepository.findAllByIdInWithOwner(countsByLessonId.keySet()).stream()
                .collect(Collectors.toMap(LessonEntity::getId, lesson -> lesson));

        List<LessonMetricRow> rows = countsByLessonId.entrySet().stream()
                .map(entry -> {
                    LessonEntity lesson = lessonsById.get(entry.getKey());
                    if (lesson == null) {
                        return null;
                    }
                    return new LessonMetricRow(
                            lesson.getId(),
                            lesson.getTitle(),
                            lesson.getUser() != null ? lesson.getUser().getId() : null,
                            lesson.getUser() != null ? lesson.getUser().getUsername() : null,
                            entry.getValue()
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(LessonMetricRow::metric, Comparator.nullsFirst(Comparator.naturalOrder())).reversed()
                        .thenComparing(LessonMetricRow::lessonId, Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();

        return buildLessonResponses(rows);
    }

    private List<LeaderboardResponse> buildContestResponses(List<ContestLeaderboardRow> rows) {
        List<LeaderboardResponse> result = new ArrayList<>();
        Integer rank = null;
        Long previousMetric = null;

        for (int i = 0; i < rows.size(); i++) {
            ContestLeaderboardRow row = rows.get(i);
            if (i == 0 || !Objects.equals(row.metric(), previousMetric)) {
                rank = i + 1;
                previousMetric = row.metric();
            }
            result.add(LeaderboardResponse.builder()
                    .rank(rank)
                    .userId(row.userId())
                    .username(row.username())
                    .rating(row.metric() != null ? row.metric().intValue() : 0)
                    .contestCount(row.contestCount() != null ? row.contestCount().intValue() : 0)
                    .build());
        }
        return result;
    }

    private List<LessonLeaderboardResponse> buildLessonResponses(List<LessonMetricRow> rows) {
        List<LessonLeaderboardResponse> result = new ArrayList<>();
        Integer rank = null;
        Long previousMetric = null;

        for (int i = 0; i < rows.size(); i++) {
            LessonMetricRow row = rows.get(i);
            if (i == 0 || !Objects.equals(row.metric(), previousMetric)) {
                rank = i + 1;
                previousMetric = row.metric();
            }
            result.add(LessonLeaderboardResponse.builder()
                    .rank(rank)
                    .lessonId(row.lessonId())
                    .title(row.title())
                    .ownerId(row.ownerId())
                    .ownerUsername(row.ownerUsername())
                    .downloadCount(row.metric() != null ? row.metric().intValue() : 0)
                    .build());
        }
        return result;
    }

    private enum ContestLeaderboardMode {
        ALL_TIME,
        LATEST_CONTEST,
        CONTEST_COUNT;

        static ContestLeaderboardMode from(String raw) {
            String value = normalize(raw);
            return switch (value) {
                case MODE_LATEST_CONTEST -> LATEST_CONTEST;
                case MODE_CONTEST_COUNT -> CONTEST_COUNT;
                default -> ALL_TIME;
            };
        }

        Long metric(UserContestStats stats) {
            return switch (this) {
                case ALL_TIME -> stats.allTimeScore();
                case LATEST_CONTEST -> stats.latestContestScore();
                case CONTEST_COUNT -> stats.contestCount();
            };
        }
    }

    private enum LessonLeaderboardRange {
        ALL_TIME,
        WEEK,
        MONTH;

        static LessonLeaderboardRange from(String raw) {
            String value = normalize(raw);
            return switch (value) {
                case RANGE_WEEK -> WEEK;
                case RANGE_MONTH -> MONTH;
                default -> ALL_TIME;
            };
        }
    }

    private static String normalize(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private record ContestLeaderboardRow(
            Long userId,
            String username,
            Long allTimeScore,
            Long contestCount,
            Long latestContestScore,
            Long metric
    ) {
    }

    private record LessonMetricRow(
            Long lessonId,
            String title,
            Long ownerId,
            String ownerUsername,
            Long metric
    ) {
    }

    private static class UserContestStats {
        private final Map<Long, ContestBucket> contestBuckets = new LinkedHashMap<>();
        private long allTimeScore = 0L;

        void addSubmission(Long contestId, int score, LocalDateTime submittedAt) {
            ContestBucket bucket = contestBuckets.computeIfAbsent(contestId, ignored -> new ContestBucket());
            bucket.add(score, submittedAt);
            allTimeScore += score;
        }

        Long latestContestScore() {
            return contestBuckets.values().stream()
                    .max(Comparator.comparing(ContestBucket::latestSubmittedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .map(ContestBucket::score)
                    .orElse(0L);
        }

        Long contestCount() {
            return (long) contestBuckets.size();
        }

        Long allTimeScore() {
            return allTimeScore;
        }

        Long scoreForContest(Long contestId) {
            if (contestId == null) return 0L;
            ContestBucket b = contestBuckets.get(contestId);
            return b != null ? b.score() : 0L;
        }
    }

    private static class ContestBucket {
        private long score = 0L;
        private LocalDateTime latestSubmittedAt;

        void add(int deltaScore, LocalDateTime submittedAt) {
            score += deltaScore;
            if (submittedAt != null && (latestSubmittedAt == null || submittedAt.isAfter(latestSubmittedAt))) {
                latestSubmittedAt = submittedAt;
            }
        }

        Long score() {
            return score;
        }

        LocalDateTime latestSubmittedAt() {
            return latestSubmittedAt;
        }
    }
}