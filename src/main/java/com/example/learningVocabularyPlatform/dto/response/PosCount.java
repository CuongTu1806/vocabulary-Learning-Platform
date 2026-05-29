package com.example.learningVocabularyPlatform.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PosCount {
    private String pos;
    private long count;
}
