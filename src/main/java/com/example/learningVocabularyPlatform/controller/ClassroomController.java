package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.config.CurrentUserResolver;
import com.example.learningVocabularyPlatform.dto.request.ClassBoardCommentRequest;
import com.example.learningVocabularyPlatform.dto.request.ClassBoardPostRequest;
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

    @GetMapping("/{id}/members/pending")
    ResponseEntity<ApiResponse> getPendingJoinRequests(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.getPendingJoinRequests(id));
    }

    @GetMapping("/{id}/board")
    ResponseEntity<ApiResponse> getClassBoard(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.getClassBoard(id));
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

    @PostMapping("/{id}/members/{userId}/approve")
    ResponseEntity<ApiResponse> approveMember(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(classroomService.approveMember(id, userId));
    }

    @PostMapping("/{id}/members/{userId}/reject")
    ResponseEntity<ApiResponse> rejectMember(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(classroomService.rejectMember(id, userId));
    }

    @PostMapping("/{id}/board")
    ResponseEntity<ApiResponse> createClassBoardPost(Authentication authentication,
                                                     @PathVariable Long id,
                                                     @RequestBody ClassBoardPostRequest request) {
        currentUserResolver.requireUserId(authentication);
        return ResponseEntity.ok(classroomService.createClassBoardPost(id, request));
    }

    @PostMapping("/{id}/board/{postId}/comments")
    ResponseEntity<ApiResponse> addClassBoardComment(Authentication authentication,
                                                     @PathVariable Long id,
                                                     @PathVariable Long postId,
                                                     @RequestBody ClassBoardCommentRequest request) {
        currentUserResolver.requireUserId(authentication);
        return ResponseEntity.ok(classroomService.addClassBoardComment(id, postId, request));
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
