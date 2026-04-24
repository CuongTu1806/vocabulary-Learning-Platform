package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.request.AssignmentRequest;
import com.example.learningVocabularyPlatform.dto.request.AssignmentSubmissionRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.FileDownloadDto;
import org.springframework.web.multipart.MultipartFile;

public interface AssignmentService {
    ApiResponse createAssignment(AssignmentRequest req);

    ApiResponse updateAssignment(AssignmentRequest req, Long id);

    ApiResponse deleteAssignment(Long id);

    ApiResponse getAssignments(Long classId);

    ApiResponse getAssignmentById(Long id);

    ApiResponse submitAssignment(Long assignmentId, AssignmentSubmissionRequest req);

    ApiResponse submitAssignmentMultipart(Long assignmentId, String content, MultipartFile[] files);

    ApiResponse uploadAssignmentAttachments(Long assignmentId, MultipartFile[] files);

    ApiResponse deleteAssignmentAttachment(Long assignmentId, Long attachmentId);

    ApiResponse getSubmissions(Long assignmentId);

    ApiResponse gradeAssignment(Long assignmentId, Long submissionId, Float score);

    FileDownloadDto downloadAssignmentAttachment(Long attachmentId);

    FileDownloadDto downloadSubmissionAttachment(Long submissionId, Long attachmentId);
}
