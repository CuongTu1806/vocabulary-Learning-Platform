package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpacedRepetitionDailySummaryResponse {
    private long learningDue;
    private long reviewDue;
    private long totalDue;
}
