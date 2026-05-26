package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.ReviewScheduleRequest;
import com.example.learningVocabularyPlatform.dto.request.ReviewSettingRequest;
import com.example.learningVocabularyPlatform.dto.response.ReviewScheduleResponse;
import com.example.learningVocabularyPlatform.dto.response.ReviewSettingResponse;
import com.example.learningVocabularyPlatform.dto.response.SpacedRepetitionCalendarDayResponse;
import com.example.learningVocabularyPlatform.dto.response.SpacedRepetitionDailySummaryResponse;
import com.example.learningVocabularyPlatform.entity.*;
import com.example.learningVocabularyPlatform.enums.RecallRating;
import com.example.learningVocabularyPlatform.repository.LessonAccessRepository;
import com.example.learningVocabularyPlatform.repository.ReviewHistoryRepository;
import com.example.learningVocabularyPlatform.repository.ReviewScheduleRepository;
import com.example.learningVocabularyPlatform.repository.ReviewSettingRepository;
import com.example.learningVocabularyPlatform.repository.UserRepository;
import com.example.learningVocabularyPlatform.repository.UserVocabularyRepository;
import com.example.learningVocabularyPlatform.service.ReviewScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewScheduleServiceImpl implements ReviewScheduleService {
	private static final String DEFAULT_LEARNING_STEPS = "1,10,30,60";
	private static final double DEFAULT_EASE_FACTOR = 2.5;
	private static final double MIN_EASE_FACTOR = 1.5;
	private static final double DEFAULT_DELAY_FACTOR = 0.05;
	private static final int DEFAULT_INTERVAL_DAYS = 1;
	private static final int MAX_INTERVAL_DAYS = 180;
	private static final double DEFAULT_EASY_BONUS = 1.3;

	private static final String STATE_NEW = "new";
	private static final String STATE_LEARNING = "learning";
	private static final String STATE_REVIEW = "review";
	private static final String STATE_RELEARNING = "relearning";
	private static final String USER_NOT_FOUND = "User not found";

	private final ReviewScheduleRepository reviewScheduleRepository;
	private final ReviewHistoryRepository reviewHistoryRepository;
	private final UserVocabularyRepository userVocabularyRepository;
	private final LessonAccessRepository lessonAccessRepository;
	private final ReviewSettingRepository reviewSettingRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public ReviewScheduleResponse startLearning(Long userId, Long userVocabularyId) {
		ReviewSettingEntity setting = getOrCreateSetting(userId);
		List<Integer> learningSteps = parseLearningSteps(setting.getLearningSteps());

		// lấy ra userVocabulary theo id
		UserVocabularyEntity userVocabulary = userVocabularyRepository
				.findById(userVocabularyId)
				.orElseThrow(() -> new IllegalArgumentException("Vocabulary not found"));
		if(userVocabulary.getVocabulary() != null) {
			VocabularyEntity vocabulary = userVocabulary.getVocabulary();
			userVocabulary.setPronunciation(vocabulary.getPronunciation());
			userVocabulary.setPos(vocabulary.getPos());
			userVocabulary.setExample(vocabulary.getExample());
			userVocabulary.setImagePath(vocabulary.getImagePath());
			userVocabulary.setExample(vocabulary.getExample());
			userVocabulary.setMeaning(vocabulary.getMeaning());
			userVocabulary.setAudioPath(vocabulary.getAudioPath());
		}
		assertUserCanAccessVocabulary(userId, userVocabulary);

		ReviewScheduleEntity schedule = reviewScheduleRepository
				.findTopByUser_IdAndUserVocabulary_IdOrderByUpdatedAtDesc(userId, userVocabularyId)
				.orElseGet(() -> createDefaultSchedule(userVocabulary));
		if (schedule.getUser() == null || !schedule.getUser().getId().equals(userId)) {
			schedule.setUser(userRepository.findById(userId)
					.orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND)));
		}

		if (STATE_NEW.equals(schedule.getState()) || schedule.getState() == null || schedule.getState().isBlank()) {
			schedule.setState(STATE_LEARNING);
			schedule.setLearningStep(1);
			int firstStepMinutes = learningSteps.getFirst();
			schedule.setIntervalDays(-firstStepMinutes);
			schedule.setNextReviewDate(LocalDateTime.now().plusMinutes(firstStepMinutes));
			schedule.setLastReviewDate(LocalDateTime.now());
			schedule.setDelayFactor(setting.getDelayFactor());
			userVocabulary.setStatus(STATE_LEARNING);
			userVocabularyRepository.save(userVocabulary);
		}

		ReviewScheduleEntity saved = reviewScheduleRepository.save(schedule);
		return toResponse(saved);
	}

	@Override
	@Transactional
	public ReviewScheduleResponse submitReview(Long userId, ReviewScheduleRequest request) {
		ReviewSettingEntity setting = getOrCreateSetting(userId);
		List<Integer> learningSteps = parseLearningSteps(setting.getLearningSteps());

		if (request == null || request.getUserVocabularyId() == null || request.getRating() == null || request.getRating().isBlank()) {
			throw new IllegalArgumentException("userVocabularyId and rating are required");
		}

		UserVocabularyEntity userVocabulary = userVocabularyRepository
				.findById(request.getUserVocabularyId())
				.orElseThrow(() -> new IllegalArgumentException("Vocabulary not found"));
		assertUserCanAccessVocabulary(userId, userVocabulary);

		ReviewScheduleEntity schedule = reviewScheduleRepository
				.findTopByUser_IdAndUserVocabulary_IdOrderByUpdatedAtDesc(userId, request.getUserVocabularyId())
				.orElseGet(() -> createDefaultSchedule(userVocabulary));
		if (schedule.getUser() == null || !schedule.getUser().getId().equals(userId)) {
			schedule.setUser(userRepository.findById(userId)
					.orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND)));
		}

		if (schedule.getState() == null || schedule.getState().isBlank()) {
			schedule.setState(STATE_NEW);
		}

		RecallRating rating = parseRating(request.getRating());
		int oldInterval = schedule.getIntervalDays();
		double oldEaseFactor = schedule.getEaseFactor() <= 0 ? DEFAULT_EASE_FACTOR : schedule.getEaseFactor();

		if (STATE_NEW.equals(schedule.getState())) {
			schedule.setState(STATE_LEARNING);
			schedule.setLearningStep(1);
		}

		if (STATE_LEARNING.equals(schedule.getState()) || STATE_RELEARNING.equals(schedule.getState())) {
			applyLearningRule(schedule, normalizeToLearningRating(rating), learningSteps);
		} else {
			applyReviewRule(schedule, rating, setting);
		}

		schedule.setLastReviewDate(LocalDateTime.now());
		ReviewScheduleEntity saved = reviewScheduleRepository.save(schedule);

		ReviewHistoryEntity history = ReviewHistoryEntity.builder()
				.rating(rating.name())
				.oldEaseFactor(oldEaseFactor)
				.newEaseFactor(saved.getEaseFactor())
				.oldIntervalDays(oldInterval)
				.newIntervalDays(saved.getIntervalDays())
				.userVocabulary(userVocabulary)
				.reviewSchedule(saved)
				.build();
		reviewHistoryRepository.save(history);

		userVocabulary.setStatus(saved.getState());
		userVocabularyRepository.save(userVocabulary);

		return toResponse(saved);
	}

	@Override
	public List<ReviewScheduleResponse> getDueCards(Long userId, int limit) {
		int safeLimit = Math.max(1, limit);
		List<ReviewScheduleEntity> dueSchedules = reviewScheduleRepository
				.findActiveReviewQueueByUserId(userId, LocalDateTime.now());

		List<ReviewScheduleResponse> responses = new ArrayList<>();
		for (ReviewScheduleEntity schedule : dueSchedules) {
			responses.add(toResponse(schedule));
			if (responses.size() >= safeLimit) {
				break;
			}
		}
		return responses;
	}

	@Override
	public SpacedRepetitionDailySummaryResponse getDailySummary(Long userId) {
		LocalDateTime now = LocalDateTime.now();
		long learningDue = reviewScheduleRepository.countActiveLearningCardsByState(userId, List.of(STATE_LEARNING));
		long relearningDue = reviewScheduleRepository.countActiveLearningCardsByState(userId, List.of(STATE_RELEARNING));
		long reviewDue = reviewScheduleRepository.countByUserVocabulary_User_IdAndNextReviewDateLessThanEqualAndStateIn(
				userId,
				now,
				List.of(STATE_REVIEW)
		);

		return SpacedRepetitionDailySummaryResponse.builder()
				.learningDue(learningDue)
				.relearningDue(relearningDue)
				.reviewDue(reviewDue)
				.totalDue(learningDue + relearningDue + reviewDue)
				.build();
	}

	@Override
	public List<SpacedRepetitionCalendarDayResponse> getMonthlyCalendar(Long userId, int year, int month) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDateTime start = LocalDateTime.of(yearMonth.atDay(1), LocalTime.MIN);
		LocalDateTime end = LocalDateTime.of(yearMonth.atEndOfMonth(), LocalTime.MAX);

		List<ReviewScheduleEntity> schedules = reviewScheduleRepository
				.findByUserVocabulary_User_IdAndNextReviewDateBetweenOrderByNextReviewDateAsc(userId, start, end);

		Map<LocalDate, Long> dayCounts = new HashMap<>();
		for (ReviewScheduleEntity schedule : schedules) {
			LocalDate day = schedule.getNextReviewDate().toLocalDate();
			dayCounts.put(day, dayCounts.getOrDefault(day, 0L) + 1);
		}

		List<SpacedRepetitionCalendarDayResponse> result = new ArrayList<>();
		for (Map.Entry<LocalDate, Long> entry : dayCounts.entrySet()) {
			result.add(SpacedRepetitionCalendarDayResponse.builder()
					.date(entry.getKey().toString())
					.dueCount(entry.getValue())
					.build());
		}

		result.sort((a, b) -> a.getDate().compareTo(b.getDate()));
		return result;
	}

	@Override
	public ReviewSettingResponse getSettings(Long userId) {
		ReviewSettingEntity setting = getOrCreateSetting(userId);
		return toSettingResponse(setting);
	}

	@Override
	@Transactional
	public ReviewSettingResponse updateSettings(Long userId, ReviewSettingRequest request) {
		ReviewSettingEntity setting = getOrCreateSetting(userId);

		if (request.getLearningSteps() != null && !request.getLearningSteps().isBlank()) {
			parseLearningSteps(request.getLearningSteps());
			setting.setLearningSteps(request.getLearningSteps().trim());
		}
		if (request.getMaxIntervalDays() != null && request.getMaxIntervalDays() > 0) {
			setting.setMaxIntervalDays(request.getMaxIntervalDays());
		}
		if (request.getEasyBonus() != null && request.getEasyBonus() > 0) {
			setting.setEasyBonus(request.getEasyBonus());
		}
		if (request.getDelayFactor() != null && request.getDelayFactor() >= 0) {
			setting.setDelayFactor(request.getDelayFactor());
		}

		ReviewSettingEntity saved = reviewSettingRepository.save(setting);
		return toSettingResponse(saved);
	}

	private ReviewScheduleEntity createDefaultSchedule(UserVocabularyEntity userVocabulary) {
		return ReviewScheduleEntity.builder()
				.userVocabulary(userVocabulary)
				.state(STATE_NEW)
				.learningStep(0)
				.repetationLevel(0)
				.intervalDays(-1)
				.easeFactor(DEFAULT_EASE_FACTOR)
				.delayFactor(DEFAULT_DELAY_FACTOR)
				.nextReviewDate(LocalDateTime.now())
				.lastReviewDate(null)
				.build();
	}

	private void assertUserCanAccessVocabulary(Long userId, UserVocabularyEntity userVocabulary) {
		if (userVocabulary.getUser() != null && userId.equals(userVocabulary.getUser().getId())) {
			return;
		}
		if (userVocabulary.getLesson() == null) {
			throw new IllegalArgumentException("Vocabulary not accessible");
		}
		if ("PUBLIC".equalsIgnoreCase(userVocabulary.getLesson().getVisibility())) {
			return;
		}
		if (lessonAccessRepository.existsByUser_IdAndLesson_Id(userId, userVocabulary.getLesson().getId())) {
			return;
		}
		throw new IllegalArgumentException("Vocabulary not accessible");
	}

	private void applyLearningRule(ReviewScheduleEntity schedule, RecallRating rating, List<Integer> learningSteps) {
		int currentStep = schedule.getLearningStep() <= 0 ? 1 : schedule.getLearningStep();

		switch (rating) {
			case FORGOT -> {
				schedule.setLearningStep(1);
				int minutes = learningSteps.getFirst();
				schedule.setIntervalDays(-minutes);
				schedule.setNextReviewDate(LocalDateTime.now().plusMinutes(minutes));
				schedule.setState(STATE_LEARNING);
			}
			case PARTIAL -> {
				int stepIndex = Math.min(currentStep - 1, learningSteps.size() - 1);
				int minutes = learningSteps.get(stepIndex);
				int retryMinutes = Math.max(1, minutes / 2);
				schedule.setIntervalDays(-retryMinutes);
				schedule.setNextReviewDate(LocalDateTime.now().plusMinutes(retryMinutes));
				schedule.setState(STATE_LEARNING);
			}
			case EFFORT -> {
				if (currentStep >= learningSteps.size()) {
					graduateToReview(schedule);
				} else {
					int nextStep = currentStep + 1;
					int nextMinutes = learningSteps.get(nextStep - 1);
					schedule.setLearningStep(nextStep);
					schedule.setIntervalDays(-nextMinutes);
					schedule.setNextReviewDate(LocalDateTime.now().plusMinutes(nextMinutes));
					schedule.setState(STATE_LEARNING);
				}
			}
			case EASY -> {
				if (currentStep >= 3) {
					graduateToReview(schedule);
				} else {
					int nextStep = currentStep + 1;
					int nextMinutes = learningSteps.get(nextStep - 1);
					schedule.setLearningStep(nextStep);
					schedule.setIntervalDays(-nextMinutes);
					schedule.setNextReviewDate(LocalDateTime.now().plusMinutes(nextMinutes));
					schedule.setState(STATE_LEARNING);
				}
			}
			default -> throw new IllegalArgumentException("Unsupported learning rating");
		}
	}

	private void applyReviewRule(ReviewScheduleEntity schedule, RecallRating rating, ReviewSettingEntity setting) {
		int quality = toSm2Quality(rating);
		double oldEF = schedule.getEaseFactor() <= 0 ? DEFAULT_EASE_FACTOR : schedule.getEaseFactor();
		int oldInterval = schedule.getIntervalDays() <= 0 ? DEFAULT_INTERVAL_DAYS : schedule.getIntervalDays();
		schedule.setDelayFactor(setting.getDelayFactor());

		double newEF = oldEF + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
		long overdueDays = 0;
		if (schedule.getNextReviewDate() != null && LocalDateTime.now().isAfter(schedule.getNextReviewDate())) {
			overdueDays = java.time.Duration.between(schedule.getNextReviewDate(), LocalDateTime.now()).toDays();
		}
		newEF -= overdueDays * schedule.getDelayFactor() * 0.01;
		newEF = Math.max(MIN_EASE_FACTOR, newEF);

		int newInterval;
		switch (rating) {
			case AGAIN -> {
				newInterval = -Math.max(1, parseLearningSteps(setting.getLearningSteps()).getFirst());
				schedule.setState(STATE_RELEARNING);
				schedule.setLearningStep(1);
				schedule.setNextReviewDate(LocalDateTime.now().plusMinutes(Math.abs(newInterval)));
			}
			case HARD -> {
				newInterval = Math.max(1, (int) Math.round(oldInterval * 1.2));
				schedule.setState(STATE_REVIEW);
				schedule.setLearningStep(0);
				schedule.setNextReviewDate(LocalDateTime.now().plusDays(newInterval));
			}
			case GOOD -> {
				newInterval = Math.max(1, (int) Math.round(oldInterval * newEF));
				schedule.setState(STATE_REVIEW);
				schedule.setLearningStep(0);
				schedule.setNextReviewDate(LocalDateTime.now().plusDays(newInterval));
			}
			case EASY -> {
				newInterval = Math.max(1, (int) Math.round(oldInterval * newEF * setting.getEasyBonus()));
				schedule.setState(STATE_REVIEW);
				schedule.setLearningStep(0);
				schedule.setNextReviewDate(LocalDateTime.now().plusDays(newInterval));
			}
			default -> throw new IllegalArgumentException("Unsupported review rating");
		}

		if (newInterval > 0) {
			newInterval = Math.min(newInterval, setting.getMaxIntervalDays());
		}
		schedule.setIntervalDays(newInterval);
		schedule.setEaseFactor(newEF);
		if (STATE_REVIEW.equals(schedule.getState())) {
			schedule.setRepetationLevel(schedule.getRepetationLevel() + 1);
			schedule.setNextReviewDate(LocalDateTime.now().plusDays(newInterval));
		}
	}

	private void graduateToReview(ReviewScheduleEntity schedule) {
		schedule.setState(STATE_REVIEW);
		schedule.setLearningStep(0);
		schedule.setRepetationLevel(Math.max(1, schedule.getRepetationLevel() + 1));
		schedule.setIntervalDays(DEFAULT_INTERVAL_DAYS);
		schedule.setNextReviewDate(LocalDateTime.now().plusDays(schedule.getIntervalDays()));
	}

	private ReviewSettingEntity getOrCreateSetting(Long userId) {
		return reviewSettingRepository.findByUser_Id(userId).orElseGet(() -> {
			UserEntity user = userRepository.findById(userId)
					.orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));
			ReviewSettingEntity created = ReviewSettingEntity.builder()
					.user(user)
					.learningSteps(DEFAULT_LEARNING_STEPS)
					.maxIntervalDays(MAX_INTERVAL_DAYS)
					.easyBonus(DEFAULT_EASY_BONUS)
					.delayFactor(DEFAULT_DELAY_FACTOR)
					.build();
			return reviewSettingRepository.save(created);
		});
	}

	private List<Integer> parseLearningSteps(String rawSteps) {
		if (rawSteps == null || rawSteps.isBlank()) {
			throw new IllegalArgumentException("learningSteps must not be empty");
		}

		List<Integer> steps = List.of(rawSteps.split(",")).stream()
				.map(String::trim)
				.filter(s -> !s.isBlank())
				.map(Integer::parseInt)
				.filter(v -> v > 0)
				.toList();

		if (steps.isEmpty()) {
			throw new IllegalArgumentException("learningSteps is invalid");
		}
		return steps;
	}

	private ReviewSettingResponse toSettingResponse(ReviewSettingEntity setting) {
		return ReviewSettingResponse.builder()
				.learningSteps(setting.getLearningSteps())
				.maxIntervalDays(setting.getMaxIntervalDays())
				.easyBonus(setting.getEasyBonus())
				.delayFactor(setting.getDelayFactor())
				.build();
	}

	private RecallRating parseRating(String rawRating) {
		try {
			return RecallRating.valueOf(rawRating.trim().toUpperCase(Locale.ROOT));
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid rating. Supported: forgot, partial, effort, easy, again, hard, good");
		}
	}

	private RecallRating normalizeToLearningRating(RecallRating rating) {
		return switch (rating) {
			case AGAIN -> RecallRating.FORGOT;
			case HARD -> RecallRating.PARTIAL;
			case GOOD -> RecallRating.EFFORT;
			default -> rating;
		};
	}

	private int toSm2Quality(RecallRating rating) {
		return switch (rating) {
			case AGAIN, FORGOT -> 0;
			case HARD, PARTIAL -> 3;
			case GOOD, EFFORT -> 4;
			case EASY -> 5;
		};
	}

	private ReviewScheduleResponse toResponse(ReviewScheduleEntity entity) {
		return ReviewScheduleResponse.builder()
				.id(entity.getId())
				.userVocabularyId(entity.getUserVocabulary() != null ? entity.getUserVocabulary().getId() : null)
				.word(entity.getUserVocabulary().getVocabulary() != null ? entity.getUserVocabulary().getVocabulary().getWord() : entity.getUserVocabulary().getWord())
				.meaning(entity.getUserVocabulary().getVocabulary() == null ? entity.getUserVocabulary().getMeaning() : entity.getUserVocabulary().getVocabulary().getMeaning())
				.state(entity.getState())
				.learningStep(entity.getLearningStep())
				.repetationLevel(entity.getRepetationLevel())
				.intervalDays(entity.getIntervalDays())
				.easeFactor(entity.getEaseFactor())
				.delayFactor(entity.getDelayFactor())
				.due(entity.getNextReviewDate())
				.lastReviewDate(entity.getLastReviewDate())
				.build();
	}
}
