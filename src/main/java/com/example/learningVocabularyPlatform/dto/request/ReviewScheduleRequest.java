package com.example.learningVocabularyPlatform.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewScheduleRequest {
	private Long userVocabularyId;
	private String rating;
}
