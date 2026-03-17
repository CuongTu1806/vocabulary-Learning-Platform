package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.dto.request.QuizRequest;
import com.example.learningVocabularyPlatform.dto.response.QuizResponse;
import com.example.learningVocabularyPlatform.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class QuizController {
    private final QuizService quizService;
    private static final Long userId = 1L;


    @PostMapping("/{lessonId}")
    public List<QuizResponse> quizDo(@PathVariable long lessonId,
                                     @RequestBody QuizRequest quizRequest) {
        return quizService.getQuiz(lessonId, userId, quizRequest);
    }
}
