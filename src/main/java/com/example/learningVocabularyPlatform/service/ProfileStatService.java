package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.response.*;
import com.example.learningVocabularyPlatform.entity.ReviewScheduleEntity;
import com.example.learningVocabularyPlatform.entity.UserEntity;
import com.example.learningVocabularyPlatform.entity.UserVocabularyEntity;
import com.example.learningVocabularyPlatform.repository.QuizResultRepository;
import com.example.learningVocabularyPlatform.repository.ReviewScheduleRepository;
import com.example.learningVocabularyPlatform.repository.UserRepository;
import com.example.learningVocabularyPlatform.repository.UserVocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileStatService {
    
    private final UserRepository userRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;
    private final QuizResultRepository quizResultRepository;

    public ProfileStatResponse getProfileStats(Long userId, String period) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        LocalDateTime periodStartDate = getPeriodStartDate(period);
        
        return ProfileStatResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt().toString())
                .currentRank("Silver")
                .currentRankPoints(2450)
                .maxRankPoints(3500)
                .maxRank("Gold")
                .contestsParticipated(24)
                .daysOnlineThisMonth(calculateDaysOnlineThisMonth(user))
                .currentStreak(calculateCurrentStreak(user))
                .reviews(getReviewsStats(user, period, periodStartDate))
                .time(getTimeStats(user, period, periodStartDate))
                .cardCount(getCardCountStats(user))
                .reviewInterval(getReviewIntervalStats(user))
                .cardEase(getCardEaseStats(user))
                .add(getAddStats(user, period, periodStartDate))
                .build();
    }

    private ReviewsStatResponse getReviewsStats(UserEntity user, String period, LocalDateTime periodStartDate) {
        // Get data for all days in the period
        List<ReviewsChartDataDto> data = generateReviewsChartData(period, user, periodStartDate);
        
        // Calculate stats from the data
        long totalReviews = data.stream()
                .mapToLong(d -> d.getLearning() + d.getRelearning() + d.getYoung() + d.getMature() + d.getFiltered())
                .sum();
        
        long daysWithActivity = data.stream()
                .filter(d -> (d.getLearning() + d.getRelearning() + d.getYoung() + d.getMature() + d.getFiltered()) > 0)
                .count();
        
        int daysTotal = getDaysTotal(period);
        long avgOverPeriod = daysTotal > 0 ? totalReviews / daysTotal : 0;
        long avgForDaysStudied = daysWithActivity > 0 ? totalReviews / daysWithActivity : 0;
        
        ReviewsChartStatsDto stats = ReviewsChartStatsDto.builder()
                .daysStudied((int) daysWithActivity)
                .daysTotal(daysTotal)
                .total(totalReviews)
                .avgOverPeriod(avgOverPeriod)
                .avgForDaysStudied(avgForDaysStudied)
                .build();
        
        return ReviewsStatResponse.builder()
                .data(data)
                .stats(stats)
                .build();
    }

    private TimeStatResponse getTimeStats(UserEntity user, String period, LocalDateTime periodStartDate) {
        // Get data for all days in the period
        List<TimeChartDataDto> data = generateTimeChartData(period, user, periodStartDate);
        
        // Calculate stats from the data
        long totalMinutes = data.stream()
                .mapToLong(d -> d.getLearning() + d.getRelearning() + d.getYoung() + d.getMature())
                .sum();
        
        long daysWithActivity = data.stream()
                .filter(d -> (d.getLearning() + d.getRelearning() + d.getYoung() + d.getMature()) > 0)
                .count();
        
        int daysTotal = getDaysTotal(period);
        long avgOverPeriod = daysTotal > 0 ? totalMinutes / daysTotal : 0;
        long avgForDaysStudied = daysWithActivity > 0 ? totalMinutes / daysWithActivity : 0;
        
        TimeChartStatsDto stats = TimeChartStatsDto.builder()
                .daysStudied((int) daysWithActivity)
                .daysTotal(daysTotal)
                .total(totalMinutes)
                .avgOverPeriod(avgOverPeriod)
                .avgForDaysStudied(avgForDaysStudied)
                .build();
        
        return TimeStatResponse.builder()
                .data(data)
                .stats(stats)
                .build();
    }

    /** Khoảng ngày tối thiểu để một thẻ ở trạng thái review được xem là “mature” (còn lại là young). */
    private static final int REVIEW_YOUNG_MAX_EXCLUSIVE_DAYS = 21;

    private static final double FALLBACK_EASE_FACTOR = 2.5;

    private static double normalizeEaseFactor(double raw) {
        if (Double.isNaN(raw) || Double.isInfinite(raw) || raw <= 0) {
            return FALLBACK_EASE_FACTOR;
        }
        return raw;
    }

    /**
     * Phân loại từ vựng trên <strong>tất cả</strong> {@code user_vocabulary} của người dùng.
     * Trước đây chỉ đếm các dòng trong {@code review_schedule}, nên từ chưa bắt đầu SRS bị thiếu
     * (nhìn như chỉ có từ vừa học trong ngày).
     */
    private CardCountStatResponse getCardCountStats(UserEntity user) {
        Long userId = user.getId();
        List<UserVocabularyEntity> allCards = userVocabularyRepository.findByUser_Id(userId);
        List<ReviewScheduleEntity> latestSchedules =
                reviewScheduleRepository.findLatestReviewForEachVocabulary(userId);

        Map<Long, ReviewScheduleEntity> scheduleByUvId = latestSchedules.stream()
                .collect(Collectors.toMap(rs -> rs.getUserVocabulary().getId(), rs -> rs, (a, b) -> b));

        long newCount = 0;
        long learning = 0;
        long relearning = 0;
        long young = 0;
        long mature = 0;

        for (UserVocabularyEntity uv : allCards) {
            ReviewScheduleEntity rs = scheduleByUvId.get(uv.getId());
            String state = rs != null && rs.getState() != null ? rs.getState().trim() : "";

            if (rs == null || state.isEmpty() || "new".equalsIgnoreCase(state)) {
                newCount++;
                continue;
            }

            switch (state.toLowerCase(Locale.ROOT)) {
                case "learning" -> learning++;
                case "relearning" -> relearning++;
                case "review" -> {
                    int interval = rs.getIntervalDays();
                    if (interval >= REVIEW_YOUNG_MAX_EXCLUSIVE_DAYS) {
                        mature++;
                    } else {
                        young++;
                    }
                }
                case "young" -> young++;
                case "mature" -> mature++;
                case "filtered" -> mature++;
                default -> newCount++;
            }
        }

        List<CardCountItemDto> data = Arrays.asList(
                CardCountItemDto.builder().name("New").value(newCount).color("#3b82f6").build(),
                CardCountItemDto.builder().name("Learning").value(learning).color("#f97316").build(),
                CardCountItemDto.builder().name("Relearning").value(relearning).color("#ef4444").build(),
                CardCountItemDto.builder().name("Young").value(young).color("#8b5cf6").build(),
                CardCountItemDto.builder().name("Mature").value(mature).color("#10b981").build()
        );

        long total = newCount + learning + relearning + young + mature;

        return CardCountStatResponse.builder()
                .data(data)
                .total(total)
                .build();
    }

    private ReviewIntervalStatResponse getReviewIntervalStats(UserEntity user) {
        // Query latest review schedule for each vocabulary to get current interval
        List<ReviewScheduleEntity> latestReviews = reviewScheduleRepository.findLatestReviewForEachVocabulary(user.getId());
        
        if (latestReviews.isEmpty()) {
            return ReviewIntervalStatResponse.builder()
                    .data(new ArrayList<>())
                    .medianInterval(0.0)
                    .total(0L)
                    .build();
        }
        
        // Get all interval values and find max
        List<Integer> intervalValues = new ArrayList<>();
        for (ReviewScheduleEntity schedule : latestReviews) {
            intervalValues.add(schedule.getIntervalDays());
        }
        
        // Calculate median
        Collections.sort(intervalValues);
        double medianInterval = intervalValues.size() % 2 == 0
                ? (intervalValues.get(intervalValues.size() / 2 - 1) + intervalValues.get(intervalValues.size() / 2)) / 2.0
                : intervalValues.get(intervalValues.size() / 2);
        
        // Find max interval
        int maxInterval = intervalValues.stream().mapToInt(Integer::intValue).max().orElse(0);
        
        // Count distribution from 0 to max days
        Map<Integer, Long> daysCounts = new TreeMap<>();
        for (int i = 0; i <= maxInterval; i++) {
            daysCounts.put(i, 0L);
        }
        
        for (Integer intervalDay : intervalValues) {
            daysCounts.put(intervalDay, daysCounts.getOrDefault(intervalDay, 0L) + 1);
        }
        
        // Build response with daily distribution
        long total = latestReviews.size();
        List<ReviewIntervalItemDto> data = daysCounts.entrySet().stream()
                .map(entry -> ReviewIntervalItemDto.builder()
                        .days(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        
        return ReviewIntervalStatResponse.builder()
                .data(data)
                .medianInterval(medianInterval)
                .total(total)
                .build();
    }

    private CardEaseStatResponse getCardEaseStats(UserEntity user) {
        // Query latest review schedule for each vocabulary to get current ease factor
        List<ReviewScheduleEntity> latestReviews = reviewScheduleRepository.findLatestReviewForEachVocabulary(user.getId());
        
        if (latestReviews.isEmpty()) {
            return CardEaseStatResponse.builder()
                    .data(new ArrayList<>())
                    .medianEaseFactor(0.0)
                    .medianEasePercent(0)
                    .build();
        }

        Map<String, Long> easeCounts = new TreeMap<>();
        List<Double> easeValues = new ArrayList<>();

        for (ReviewScheduleEntity schedule : latestReviews) {
            double ease = normalizeEaseFactor(schedule.getEaseFactor());
            easeValues.add(ease);

            String easeLabel = formatEase(ease);
            easeCounts.put(easeLabel, easeCounts.getOrDefault(easeLabel, 0L) + 1);
        }

        Collections.sort(easeValues);
        double medianFactor = easeValues.size() % 2 == 0
                ? (easeValues.get(easeValues.size() / 2 - 1) + easeValues.get(easeValues.size() / 2)) / 2.0
                : easeValues.get(easeValues.size() / 2);

        int medianPercent = (int) Math.round(medianFactor * 100.0);

        List<CardEaseItemDto> data = easeCounts.entrySet().stream()
                .map(entry -> CardEaseItemDto.builder()
                        .ease(entry.getKey())
                        .count(entry.getValue())
                        .color(getEaseColor(entry.getKey()))
                        .build())
                .collect(Collectors.toList());

        return CardEaseStatResponse.builder()
                .data(data)
                .medianEaseFactor(medianFactor)
                .medianEasePercent(medianPercent)
                .build();
    }
    
    private String formatEase(double ease) {
        int easeInt = (int) (ease * 100);
        return easeInt + "%";
    }
    
    private String getEaseColor(String easeLabel) {
        try {
            int ease = Integer.parseInt(easeLabel.replace("%", ""));
            if (ease < 150) return "#ef4444"; // red
            if (ease < 180) return "#f97316"; // orange
            if (ease < 200) return "#eab308"; // yellow
            if (ease < 220) return "#3b82f6"; // blue
            if (ease < 240) return "#8b5cf6"; // purple
            if (ease < 260) return "#06b6d4"; // cyan
            return "#10b981"; // green
        } catch (NumberFormatException e) {
            return "#6b7280"; // gray default
        }
    }

    private AddStatResponse getAddStats(UserEntity user, String period, LocalDateTime periodStartDate) {
        List<AddChartDataDto> data = generateAddChartData(period, user, periodStartDate);
        Long total = data.stream().mapToLong(AddChartDataDto::getAdded).sum();
        Long average = data.size() > 0 ? Math.round((double) total / data.size()) : 0;
        
        AddChartStatsDto stats = AddChartStatsDto.builder()
                .total(total)
                .average(average)
                .period(period)
                .build();
        
        return AddStatResponse.builder()
                .data(data)
                .stats(stats)
                .build();
    }

    private List<ReviewsChartDataDto> generateReviewsChartData(String period, UserEntity user, LocalDateTime periodStartDate) {
        List<ReviewsChartDataDto> data = new ArrayList<>();
        int daysCount = getDaysTotal(period);
        LocalDateTime now = LocalDateTime.now();
        
        // Generate data for each day in the period
        for (int i = -daysCount; i <= 0; i++) {
            LocalDateTime dayStart = now.minusDays(-i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59);
            
            // Query reviews for this specific day
            List<ReviewScheduleEntity> dayReviews = reviewScheduleRepository.findReviewsByDateRange(
                    user.getId(),
                    dayStart,
                    dayEnd
            );
            
            // Count by state
            long learning = dayReviews.stream().filter(r -> "learning".equalsIgnoreCase(r.getState())).count();
            long relearning = dayReviews.stream().filter(r -> "relearning".equalsIgnoreCase(r.getState())).count();
            long young = dayReviews.stream().filter(r -> "young".equalsIgnoreCase(r.getState())).count();
            long mature = dayReviews.stream().filter(r -> "mature".equalsIgnoreCase(r.getState())).count();
            long filtered = dayReviews.stream().filter(r -> "filtered".equalsIgnoreCase(r.getState())).count();
            
            data.add(ReviewsChartDataDto.builder()
                    .day(String.valueOf(i))
                    .learning(learning)
                    .relearning(relearning)
                    .young(young)
                    .mature(mature)
                    .filtered(filtered)
                    .build());
        }
        
        return data;
    }

    private List<TimeChartDataDto> generateTimeChartData(String period, UserEntity user, LocalDateTime periodStartDate) {
        List<TimeChartDataDto> data = new ArrayList<>();
        int daysCount = getDaysTotal(period);
        LocalDateTime now = LocalDateTime.now();
        
        // Generate data for each day in the period
        for (int i = -daysCount; i <= 0; i++) {
            LocalDateTime dayStart = now.minusDays(-i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59);
            
            // Query quiz results for this specific day and group by card state
            // We estimate time by assuming 2 minutes per review based on quiz results
            List<ReviewScheduleEntity> dayReviews = reviewScheduleRepository.findReviewsByDateRange(
                    user.getId(),
                    dayStart,
                    dayEnd
            );
            
            // Count by state and estimate time (2 minutes per review as baseline)
            long learning = dayReviews.stream().filter(r -> "learning".equalsIgnoreCase(r.getState())).count() * 2;
            long relearning = dayReviews.stream().filter(r -> "relearning".equalsIgnoreCase(r.getState())).count() * 2;
            long young = dayReviews.stream().filter(r -> "young".equalsIgnoreCase(r.getState())).count() * 2;
            long mature = dayReviews.stream().filter(r -> "mature".equalsIgnoreCase(r.getState())).count() * 2;
            
            data.add(TimeChartDataDto.builder()
                    .day(String.valueOf(i))
                    .learning(learning)
                    .relearning(relearning)
                    .young(young)
                    .mature(mature)
                    .build());
        }
        
        return data;
    }

    private List<AddChartDataDto> generateAddChartData(String period, UserEntity user, LocalDateTime periodStartDate) {
        List<AddChartDataDto> data = new ArrayList<>();
        int daysCount = getDaysTotal(period);
        LocalDateTime now = LocalDateTime.now();
        
        // Generate data for each day in the period
        for (int i = -daysCount; i <= 0; i++) {
            LocalDateTime dayStart = now.minusDays(-i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59);
            
            // Query new vocabulary added on this day
            List<UserVocabularyEntity> dayVocabs = userVocabularyRepository.findByUserIdAndDateRange(
                    user.getId(),
                    dayStart,
                    dayEnd
            );
            
            data.add(AddChartDataDto.builder()
                    .day(String.valueOf(i))
                    .added((long) dayVocabs.size())
                    .build());
        }
        
        return data;
    }

    private LocalDateTime getPeriodStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case "month" -> now.minusMonths(1);
            case "quarter" -> now.minusMonths(3);
            case "year" -> now.minusYears(1);
            default -> now.minusMonths(1);
        };
    }

    private Integer getDaysTotal(String period) {
        return switch (period) {
            case "month" -> 31;
            case "quarter" -> 90;
            case "year" -> 365;
            default -> 31;
        };
    }


    private Integer calculateDaysOnlineThisMonth(UserEntity user) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        // Query: count distinct dates with review activities in last 30 days
        List<ReviewScheduleEntity> schedules = reviewScheduleRepository.findByUserVocabulary_User_IdAndLastReviewDateAfter(
                user.getId(),
                thirtyDaysAgo
        );
        
        // Count unique dates
        long uniqueDays = schedules.stream()
                .map(s -> s.getLastReviewDate().toLocalDate())
                .distinct()
                .count();
        
        return (int) uniqueDays;
    }

    /**
     * Calculate current study streak (consecutive days with activity from today backwards)
     */
    private Integer calculateCurrentStreak(UserEntity user) {
        LocalDateTime today = LocalDateTime.now();
        int streak = 0;
        
        // Check backwards from today
        for (int i = 0; i < 365; i++) {
            LocalDateTime dayStart = today.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59);
            
            List<ReviewScheduleEntity> dayActivity = reviewScheduleRepository.findByUserVocabulary_User_IdAndLastReviewDateBetween(
                    user.getId(),
                    dayStart,
                    dayEnd
            );
            
            if (dayActivity.isEmpty()) {
                break; // Streak ended
            }
            streak++;
        }
        
        return streak;
    }
}
