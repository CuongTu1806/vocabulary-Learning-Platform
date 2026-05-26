package com.example.learningVocabularyPlatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TimeChartDataDto {
    private String day;
    private Long learning;   // minutes
    private Long relearning; // minutes
    private Long young;      // minutes
    private Long mature;     // minutes
}
