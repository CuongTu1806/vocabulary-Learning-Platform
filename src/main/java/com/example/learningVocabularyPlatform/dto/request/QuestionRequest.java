package com.example.learningVocabularyPlatform.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequest {
    private String text;
    private String type; // "mcq" | "fill"
    private List<ChoiceRequest> choices; // for mcq
    private String answer; // for fill
}
