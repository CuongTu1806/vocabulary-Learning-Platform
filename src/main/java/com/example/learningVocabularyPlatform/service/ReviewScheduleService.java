package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.request.ReviewScheduleRequest;
import com.example.learningVocabularyPlatform.dto.request.ReviewSettingRequest;
import com.example.learningVocabularyPlatform.dto.response.ReviewScheduleResponse;
import com.example.learningVocabularyPlatform.dto.response.SpacedRepetitionCalendarDayResponse;
import com.example.learningVocabularyPlatform.dto.response.SpacedRepetitionDailySummaryResponse;
import com.example.learningVocabularyPlatform.dto.response.ReviewSettingResponse;

import java.util.List;

public interface ReviewScheduleService {
	ReviewScheduleResponse startLearning(Long userId, Long userVocabularyId);

	ReviewScheduleResponse submitReview(Long userId, ReviewScheduleRequest request);

	List<ReviewScheduleResponse> getDueCards(Long userId, int limit);

	SpacedRepetitionDailySummaryResponse getDailySummary(Long userId);

	List<SpacedRepetitionCalendarDayResponse> getMonthlyCalendar(Long userId, int year, int month);

	ReviewSettingResponse getSettings(Long userId);

	ReviewSettingResponse updateSettings(Long userId, ReviewSettingRequest request);
}
