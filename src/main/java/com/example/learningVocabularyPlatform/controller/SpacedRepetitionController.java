package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.dto.request.ReviewScheduleRequest;
import com.example.learningVocabularyPlatform.dto.request.ReviewSettingRequest;
import com.example.learningVocabularyPlatform.dto.response.ReviewScheduleResponse;
import com.example.learningVocabularyPlatform.dto.response.ReviewSettingResponse;
import com.example.learningVocabularyPlatform.dto.response.SpacedRepetitionCalendarDayResponse;
import com.example.learningVocabularyPlatform.dto.response.SpacedRepetitionDailySummaryResponse;
import com.example.learningVocabularyPlatform.service.ReviewScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spaced_repetition")
public class SpacedRepetitionController {
    private static final Long USER_ID = 1L;
    private final ReviewScheduleService reviewScheduleService;

    @PostMapping("/start/{userVocabularyId}")
    public ReviewScheduleResponse startLearning(@PathVariable Long userVocabularyId) {
        return reviewScheduleService.startLearning(USER_ID, userVocabularyId);
    }

    @PostMapping("/answer")
    public ReviewScheduleResponse submitAnswer(@RequestBody ReviewScheduleRequest request) {
        return reviewScheduleService.submitReview(USER_ID, request);
    }

    @GetMapping("/due")
    public List<ReviewScheduleResponse> getDueCards(@RequestParam(defaultValue = "20") int limit) {
        return reviewScheduleService.getDueCards(USER_ID, limit);
    }

    @GetMapping("/summary")
    public SpacedRepetitionDailySummaryResponse getDailySummary() {
        return reviewScheduleService.getDailySummary(USER_ID);
    }

    @GetMapping("/calendar")
    public List<SpacedRepetitionCalendarDayResponse> getMonthlyCalendar(@RequestParam(required = false) Integer year,
                                                                         @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int safeYear = year == null ? now.getYear() : year;
        int safeMonth = month == null ? now.getMonthValue() : month;
        return reviewScheduleService.getMonthlyCalendar(USER_ID, safeYear, safeMonth);
    }

    @GetMapping("/settings")
    public ReviewSettingResponse getSettings() {
        return reviewScheduleService.getSettings(USER_ID);
    }

    @PutMapping("/settings")
    public ReviewSettingResponse updateSettings(@RequestBody ReviewSettingRequest request) {
        return reviewScheduleService.updateSettings(USER_ID, request);
    }


}
