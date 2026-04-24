package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.dto.request.AssignmentRequest;
import com.example.learningVocabularyPlatform.dto.request.AssignmentSubmissionRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.FileDownloadDto;
import com.example.learningVocabularyPlatform.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody AssignmentRequest req) {
        return ResponseEntity.ok(assignmentService.createAssignment(req));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
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

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAssignmentAttachment(@PathVariable Long attachmentId) {
        FileDownloadDto d = assignmentService.downloadAssignmentAttachment(attachmentId);
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(d.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(MediaType.parseMediaType(d.getContentType()))
                .body(d.getResource());
    }

    @GetMapping("/submissions/{submissionId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadSubmissionAttachment(
            @PathVariable Long submissionId,
            @PathVariable Long attachmentId) {
        FileDownloadDto d = assignmentService.downloadSubmissionAttachment(submissionId, attachmentId);
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(d.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(MediaType.parseMediaType(d.getContentType()))
                .body(d.getResource());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }

    @PostMapping(value = "/{id}/submit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> submit(
            @Valid @RequestBody AssignmentSubmissionRequest req, @PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.submitAssignment(id, req));
    }

    @PostMapping(value = "/{id}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> submitMultipart(
            @PathVariable Long id,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        return ResponseEntity.ok(assignmentService.submitAssignmentMultipart(id, content, files));
    }

    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadAttachments(
            @PathVariable Long id, @RequestParam("files") MultipartFile[] files) {
        return ResponseEntity.ok(assignmentService.uploadAssignmentAttachments(id, files));
    }

    @DeleteMapping("/{assignmentId}/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse> deleteAttachment(
            @PathVariable Long assignmentId, @PathVariable Long attachmentId) {
        return ResponseEntity.ok(assignmentService.deleteAssignmentAttachment(assignmentId, attachmentId));
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<ApiResponse> getSubmissions(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getSubmissions(id));
    }

    @PutMapping("/{assignmentId}/submissions/{submissionId}/grade")
    public ResponseEntity<ApiResponse> gradeAssignment(
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            @RequestParam Float score) {
        return ResponseEntity.ok(assignmentService.gradeAssignment(assignmentId, submissionId, score));
    }
}
