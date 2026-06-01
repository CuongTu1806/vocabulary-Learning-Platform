package com.example.learningVocabularyPlatform.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {
    private Long id;
    private Long classId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Long createdByUserId;
    private String type;
    private java.util.List<com.example.learningVocabularyPlatform.dto.response.QuestionResponse> questions;
    /** Gắn khi GET chi tiết: chủ lớp / người tạo bài */
    private Boolean currentUserCanGrade;
    /** Gắn khi GET chi tiết: thành viên đã nộp chưa */
    private Boolean currentUserHasSubmitted;
    /** Gắn khi GET chi tiết: bài nộp của user hiện tại (nếu có) */
    private com.example.learningVocabularyPlatform.dto.response.AssignmentSubmissionResponse currentUserSubmission;
    /** File đính kèm đề bài (GET chi tiết) */
    private List<AssignmentAttachmentResponse> attachments;
}
