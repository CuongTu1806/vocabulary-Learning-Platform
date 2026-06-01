package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {
    private String text;
    private String type;
    private List<ChoiceResponse> choices;
    private String answer;
}
