package com.example.learningVocabularyPlatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReviewIntervalStatResponse {
    private List<ReviewIntervalItemDto> data;
    private Double medianInterval;
    private Long total;
}
