package com.example.learningVocabularyPlatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReviewsChartStatsDto {
    private Integer daysStudied;
    private Integer daysTotal;
    private Long total;
    private Long avgOverPeriod;
    private Long avgForDaysStudied;
}
