package com.example.learningVocabularyPlatform.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {
    private Long classId;
    private String title;
    private String description;

    @NotNull(message = "ngay khong duoc trong")
    @Future
    private LocalDateTime dueDate;
}
