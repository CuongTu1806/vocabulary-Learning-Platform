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
@RequestMapping("/api/lessons")
public class LessonController {
    private final LessonService lessonService;
    private static final Long userId = 1L;
    private final UserVocabularyService userVocabularyService;

    // Get all lesson belong to user
    @GetMapping("")
    public List<LessonResponse> getAllLessons() {
        return lessonService.getAll(userId);
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

    // hiển thị toàn bộ vocab trong lesson
    @GetMapping("/{lessonId}/vocabularies")
    public List<UserVocabularyResponse> getVocabInLesson(@PathVariable Long lessonId){
        return lessonService.getVocabInLesson(lessonId);
    }

    // update a vocabulary in lesson, system vocab can not be edited
    @PutMapping("/{lessonId}/vocabularies/{vocabId}")
    public UserVocabularyResponse updateVocabInLesson(@PathVariable Long lessonId,
                                              @PathVariable Long vocabId,
                                              @RequestBody VocabularyAddRequest request) {
        return userVocabularyService.updateVocabInLesson(vocabId, request);
    }

    // delete a vocab in lesson
    @DeleteMapping("/{lessonId}/vocabularies/{vocabId}")
    public void deleteVocabInLesson(@PathVariable Long lessonId, @PathVariable Long vocabId) {
        userVocabularyService.deleteVocabInLesson(vocabId);
    }
}
