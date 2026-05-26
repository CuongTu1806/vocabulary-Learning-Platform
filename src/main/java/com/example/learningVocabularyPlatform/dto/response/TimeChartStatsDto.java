package com.example.learningVocabularyPlatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TimeChartStatsDto {
    private Integer daysStudied;
    private Integer daysTotal;
    private Long total;      // minutes
    private Long avgOverPeriod;      // minutes per day
    private Long avgForDaysStudied;  // minutes per study day
}
