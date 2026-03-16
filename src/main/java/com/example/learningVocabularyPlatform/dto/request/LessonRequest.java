package com.example.learningVocabularyPlatform.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class LessonRequest {
    private String title;
    private String description;
}
