package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.dto.request.LessonRequest;
import com.example.learningVocabularyPlatform.dto.request.VocabularyAddRequest;
import com.example.learningVocabularyPlatform.dto.response.LessonResponse;
import com.example.learningVocabularyPlatform.dto.response.UserVocabularyResponse;
import com.example.learningVocabularyPlatform.service.LessonService;
import com.example.learningVocabularyPlatform.service.UserVocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lesson")
public class LessonController {
    private final LessonService lessonService;
    private final UserVocabularyService userVocabularyService;
    private static final Long userId = 1L;

    // Get all lesson belong to user
    @GetMapping("")
    public List<LessonResponse> getAllLessons() {
        return lessonService.getAll(userId);
    }

    // hiển thị toàn bộ vocab trong lesson
    @GetMapping("/{lessonId}")
    public List<UserVocabularyResponse> getVocabInLesson(@PathVariable Long lessonId){
        return userVocabularyService.getVocabInLesson(lessonId);
    }

    // create lesson
    @PostMapping("")
    public LessonResponse createLesson(@RequestBody LessonRequest lessonRequest) {
        return lessonService.createLesson(userId, lessonRequest);
    }

    // update lesson
    @PutMapping("/{lessonId}")
    public LessonResponse updateLesson(@PathVariable Long lessonId, @RequestBody LessonRequest lessonRequest) {
        return lessonService.updateLesson(userId, lessonId, lessonRequest);
    }

    // delete lesson
    @DeleteMapping("/{lessonId}")
    public void deleteLesson(@PathVariable Long lessonId) {
        lessonService.deleteLesson(userId, lessonId);
    }

    // add vocab, only vocab belong to user
    @PostMapping("{lessonId}/vocabularies")
    public UserVocabularyResponse addVocab(@PathVariable Long lessonId,
                                           @RequestBody VocabularyAddRequest request) {
        return lessonService.addVocab(lessonId, request, userId);
    }
}
