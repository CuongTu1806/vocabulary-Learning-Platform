package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.dto.request.ContestRequest;
import com.example.learningVocabularyPlatform.dto.request.ContestSingleAnswerRequest;
import com.example.learningVocabularyPlatform.dto.request.ContestSubmitRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.FileDownloadDto;
import com.example.learningVocabularyPlatform.service.ContestService;
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

/**
 * REST API contest — khớp spec nhóm: CRUD, register, submit, ranking.
 * <p>
 * Update (PUT) không dùng {@code @Valid} để cho phép partial body; rule kiểm tra nằm trong service.
 */
@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody ContestRequest req) {
        return ResponseEntity.ok(contestService.createContest(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody ContestRequest req) {
        return ResponseEntity.ok(contestService.updateContest(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.deleteContest(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> list() {
        return ResponseEntity.ok(contestService.getContests());
    }

    @PostMapping(value = "/{contestId}/problems/{problemId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadProblemImage(
            @PathVariable Long contestId,
            @PathVariable Long problemId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(contestService.uploadProblemImage(contestId, problemId, file));
    }

    @GetMapping("/{contestId}/problems/{problemId}/image")
    public ResponseEntity<Resource> downloadProblemImage(
            @PathVariable Long contestId, @PathVariable Long problemId) {
        FileDownloadDto d = contestService.downloadProblemImage(contestId, problemId);
        ContentDisposition cd = ContentDisposition.inline()
                .filename(d.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(MediaType.parseMediaType(d.getContentType()))
                .body(d.getResource());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getContestById(id));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<ApiResponse> register(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.registerContest(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse> submit(
            @PathVariable Long id,
            @Valid @RequestBody ContestSubmitRequest req) {
        return ResponseEntity.ok(contestService.submitContest(id, req));
    }

    /** Nộp đúng một câu — phản hồi đúng/sai + tổng điểm (UI hiệu ứng từng bước). */
    @PostMapping("/{contestId}/problems/{problemId}/answer")
    public ResponseEntity<ApiResponse> submitOne(
            @PathVariable Long contestId,
            @PathVariable Long problemId,
            @Valid @RequestBody ContestSingleAnswerRequest req) {
        return ResponseEntity.ok(contestService.submitSingleAnswer(contestId, problemId, req));
    }

    /** Điểm, hạng, số câu đã làm — cho góc màn hình (điểm + đồng hồ FE + leaderboard poll /ranking). */
    @GetMapping("/{id}/me")
    public ResponseEntity<ApiResponse> myStats(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getMyContestStats(id));
    }

    @GetMapping("/{id}/ranking")
    public ResponseEntity<ApiResponse> ranking(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getRanking(id));
    }
}
