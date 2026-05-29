package com.example.learningVocabularyPlatform.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ReviewStatsDayResponse {
    private String date; // yyyy-MM-dd
    private long totalReviews;
    private long totalDurationSeconds;
    private List<PosCount> posBreakdown;
}
