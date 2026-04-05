package com.example.learningVocabularyPlatform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestAnswerItem {

    @NotNull
    private Long problemId;

    private String userAnswer;
}
