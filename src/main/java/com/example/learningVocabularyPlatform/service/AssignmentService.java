package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.request.AssignmentRequest;
import com.example.learningVocabularyPlatform.dto.request.AssignmentSubmissionRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;

public interface AssignmentService {
    ApiResponse createAssignment(AssignmentRequest req);
    ApiResponse updateAssignment(AssignmentRequest req, Long id);
    ApiResponse deleteAssignment(Long id);
    ApiResponse getAssignments(Long classId);
    ApiResponse getAssignmentById(Long id);
    ApiResponse submitAssignment(Long assignmentId, AssignmentSubmissionRequest req);
    ApiResponse getSubmissions(Long assignmentId);
    ApiResponse gradeAssignment(Long assignmentId, Long submissionId, Float score);
}
