package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.config.CurrentUserResolver;
import com.example.learningVocabularyPlatform.dto.request.ClassMemberRequest;
import com.example.learningVocabularyPlatform.dto.request.ClassroomRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/classes")
public class ClassroomController {

    private final ClassroomService classroomService;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping
    ResponseEntity<ApiResponse> listMyClassrooms() {
        return ResponseEntity.ok(classroomService.getMyClassrooms());
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse> getClassroomById(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.getClassroomById(id));
    }

    @GetMapping("/{id}/members")
    ResponseEntity<ApiResponse> getClassroomMembers(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.getClassroomMembers(id));
    }

    @PostMapping("")
    ResponseEntity<ApiResponse> createClassroom(Authentication authentication,
                                                @RequestBody ClassroomRequest classroomRequest) {
        Long currentUserId = currentUserResolver.requireUserId(authentication);
        return ResponseEntity.ok(classroomService.createClassroom(currentUserId, classroomRequest));
    }

    @PostMapping("/{id}/join")
    ResponseEntity<ApiResponse> joinClassroom(Authentication authentication, @PathVariable Long id) {
        Long currentUserId = currentUserResolver.requireUserId(authentication);
        return ResponseEntity.ok(classroomService.joinClassroom(id, currentUserId));
    }

    @PostMapping("/{id}/leave")
    ResponseEntity<ApiResponse> leaveClassroom(Authentication authentication, @PathVariable Long id) {
        Long currentUserId = currentUserResolver.requireUserId(authentication);
        return ResponseEntity.ok(classroomService.leaveClassroom(id, currentUserId));
    }

    @PostMapping("/{id}/invite")
    ResponseEntity<ApiResponse> inviteMembers(@PathVariable Long id, @RequestBody ClassMemberRequest classMemberRequest) {
        return ResponseEntity.ok(classroomService.inviteMembers(id, classMemberRequest));
    }

    @PutMapping("/{id}")
    ResponseEntity<ApiResponse> updateClassroom(@PathVariable Long id, @RequestBody ClassroomRequest classroomRequest) {
        return ResponseEntity.ok(classroomService.updateClassroom(id, classroomRequest));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse> deleteClassroom(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.deleteClassroom(id));
    }

    @DeleteMapping("/{id}/members/{userId}")
    ResponseEntity<ApiResponse> deleteMembers(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(classroomService.removeMember(id, userId));
    }

}
