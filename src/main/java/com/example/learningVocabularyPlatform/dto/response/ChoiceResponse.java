package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceResponse {
    private String text;
    private Boolean correct;
}
