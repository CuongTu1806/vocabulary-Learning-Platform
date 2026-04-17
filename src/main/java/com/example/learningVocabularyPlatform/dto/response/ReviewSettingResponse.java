package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSettingResponse {
    private String learningSteps;
    private int maxIntervalDays;
    private double easyBonus;
    private double delayFactor;
}
