package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.dto.request.AssignmentRequest;
import com.example.learningVocabularyPlatform.dto.request.AssignmentSubmissionRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody AssignmentRequest req) {
        return ResponseEntity.ok(assignmentService.createAssignment(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@Valid @RequestBody AssignmentRequest req, @PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.updateAssignment(req, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.deleteAssignment(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAssignments(@RequestParam(required = false) Long classId) {
        return ResponseEntity.ok(assignmentService.getAssignments(classId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse> submit(@RequestBody AssignmentSubmissionRequest req, @PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.submitAssignment(id, req));
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<ApiResponse> getSubmissions(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getSubmissions(id));
    }

    @PutMapping("/{assignmentId}/submissions/{submissionId}/grade")
    public ResponseEntity<ApiResponse> gradeAssignment(
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            @RequestParam Float score
    ) {
        return ResponseEntity.ok(assignmentService.gradeAssignment(assignmentId, submissionId, score));
    }
}
