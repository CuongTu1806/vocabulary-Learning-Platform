package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpacedRepetitionCalendarDayResponse {
    private String date;
    private long dueCount;
}
