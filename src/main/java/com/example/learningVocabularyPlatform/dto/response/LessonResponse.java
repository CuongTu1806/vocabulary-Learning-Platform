package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LessonResponse {
    private Long id;
    private String title;
    private String description;
    private int numberOfWords;
    private LocalDateTime createdAt;
}
