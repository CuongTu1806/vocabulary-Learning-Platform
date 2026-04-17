package com.example.learningVocabularyPlatform.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSettingRequest {
    private String learningSteps;
    private Integer maxIntervalDays;
    private Double easyBonus;
    private Double delayFactor;
}
