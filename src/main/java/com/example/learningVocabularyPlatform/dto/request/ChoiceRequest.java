package com.example.learningVocabularyPlatform.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceRequest {
    private String text;
    private Boolean correct;
}
