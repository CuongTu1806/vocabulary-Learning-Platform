package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.config.CurrentUserResolver;
import com.example.learningVocabularyPlatform.dto.request.ReviewScheduleRequest;
import com.example.learningVocabularyPlatform.dto.request.ReviewSettingRequest;
import com.example.learningVocabularyPlatform.dto.response.ReviewScheduleResponse;
import com.example.learningVocabularyPlatform.dto.response.ReviewSettingResponse;
import com.example.learningVocabularyPlatform.dto.response.SpacedRepetitionCalendarDayResponse;
import com.example.learningVocabularyPlatform.dto.response.SpacedRepetitionDailySummaryResponse;
import com.example.learningVocabularyPlatform.service.ReviewScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spaced_repetition")
public class SpacedRepetitionController {
    private final ReviewScheduleService reviewScheduleService;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/start/{userVocabularyId}")
    public ReviewScheduleResponse startLearning(Authentication authentication,
                                                @PathVariable Long userVocabularyId) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return reviewScheduleService.startLearning(userId, userVocabularyId);
    }

    @PostMapping("/answer")
    public ReviewScheduleResponse submitAnswer(Authentication authentication,
                                               @RequestBody ReviewScheduleRequest request) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return reviewScheduleService.submitReview(userId, request);
    }

    @GetMapping("/due")
    public List<ReviewScheduleResponse> getDueCards(Authentication authentication,
                                                    @RequestParam(defaultValue = "20") int limit) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return reviewScheduleService.getDueCards(userId, limit);
    }

    @GetMapping("/summary")
    public SpacedRepetitionDailySummaryResponse getDailySummary(Authentication authentication) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return reviewScheduleService.getDailySummary(userId);
    }

    @GetMapping("/calendar")
    public List<SpacedRepetitionCalendarDayResponse> getMonthlyCalendar(Authentication authentication,
                                                                         @RequestParam(required = false) Integer year,
                                                                         @RequestParam(required = false) Integer month) {
        Long userId = currentUserResolver.requireUserId(authentication);
        LocalDate now = LocalDate.now();
        int safeYear = year == null ? now.getYear() : year;
        int safeMonth = month == null ? now.getMonthValue() : month;
        return reviewScheduleService.getMonthlyCalendar(userId, safeYear, safeMonth);
    }

    @GetMapping("/settings")
    public ReviewSettingResponse getSettings(Authentication authentication) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return reviewScheduleService.getSettings(userId);
    }

    @PutMapping("/settings")
    public ReviewSettingResponse updateSettings(Authentication authentication,
                                                @RequestBody ReviewSettingRequest request) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return reviewScheduleService.updateSettings(userId, request);
    }


}
