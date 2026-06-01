package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.config.CurrentUserResolver;
import com.example.learningVocabularyPlatform.dto.request.QuizHistoryRequest;
import com.example.learningVocabularyPlatform.dto.request.QuizRequest;
import com.example.learningVocabularyPlatform.dto.request.QuizSubmitRequest;
import com.example.learningVocabularyPlatform.dto.response.QuizHistoryResponse;
import com.example.learningVocabularyPlatform.dto.response.QuizResponse;
import com.example.learningVocabularyPlatform.dto.response.QuizResultDetailResponse;
import com.example.learningVocabularyPlatform.dto.response.QuizSubmitResponse;
import com.example.learningVocabularyPlatform.service.QuizResultService;
import com.example.learningVocabularyPlatform.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class QuizController {
    private final QuizService quizService;
    private final QuizResultService quizResultService;
    private final CurrentUserResolver currentUserResolver;

    // create quiz
    @PostMapping("/{lessonId}")
    public QuizResponse quizDo(Authentication authentication,
                               @PathVariable long lessonId,
                                     @RequestBody QuizRequest quizRequest) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return quizService.getQuiz(lessonId, userId, quizRequest);
    }

    // submit quiz
    @PostMapping("/submit")
    public QuizSubmitResponse submitQuiz(Authentication authentication,
                                         @RequestBody QuizSubmitRequest quizSubmitRequest) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return quizService.submitQuiz(userId, quizSubmitRequest);
    }

    @GetMapping("/history")
    public Page<QuizHistoryResponse> getQuizHistory(Authentication authentication,
                                                    QuizHistoryRequest quizHistoryRequest,
                                                    Pageable pageable) {
        Long userId = currentUserResolver.requireUserId(authentication);
        return quizService.getQuizHistory(userId, quizHistoryRequest, pageable);
    }

    @GetMapping("/history/{quizId}")
    public List<QuizResultDetailResponse> getQuizResult(@PathVariable Long quizId) {
        return quizResultService.getDetail(quizId);
    }
}
