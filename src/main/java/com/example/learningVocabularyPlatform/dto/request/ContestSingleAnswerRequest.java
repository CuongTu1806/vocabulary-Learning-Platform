package com.example.learningVocabularyPlatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestSingleAnswerRequest {

    @NotBlank(message = "Nhập đáp án")
    private String userAnswer;
}
