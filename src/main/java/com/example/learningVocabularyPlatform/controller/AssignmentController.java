package com.example.learningVocabularyPlatform.controller;
//	/api/assignments
//Create assignment	POST /api/assignments
//Update assignment	PUT /api/assignments/{id}
//Delete assignment	DELETE /api/assignments/{id}
//Get assignments	GET /api/assignments
//Submit assignment	POST /api/assignments/{id}/submit
//Get submission	GET /api/assignments/{id}/submissions


import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

//    @PostMapping
//    public ResponseEntity<ApiResponse> createAssignment() {
//        return ResponseEntity.ok(assignmentService.createAssignment());
//    }
}
