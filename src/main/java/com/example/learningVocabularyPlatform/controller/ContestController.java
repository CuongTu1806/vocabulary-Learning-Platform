package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.dto.request.ContestRequest;
import com.example.learningVocabularyPlatform.dto.request.ContestSubmitRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.service.ContestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}/ranking")
    public ResponseEntity<ApiResponse> ranking(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getRanking(id));
    }
}
