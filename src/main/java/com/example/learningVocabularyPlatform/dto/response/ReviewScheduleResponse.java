package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewScheduleResponse {
	private Long id;
	private Long userVocabularyId;
	private String word;
	private String meaning;
	private String state;
	private int learningStep;
	private int repetationLevel;
	private int intervalDays;
	private double easeFactor;
	private double delayFactor;
	private LocalDateTime due;
	private LocalDateTime lastReviewDate;
}
