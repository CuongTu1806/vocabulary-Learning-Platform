package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.request.ClassMemberRequest;
import com.example.learningVocabularyPlatform.dto.request.ClassroomRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;

public interface ClassroomService {
    // Create class
    ApiResponse createClassroom(ClassroomRequest classroomRequest);

    // Update class
    ApiResponse updateClassroom(Long id, ClassroomRequest classroomRequest);

    // Delete class
    ApiResponse deleteClassroom(Long id);

    // Join class
    ApiResponse joinClassroom(Long id);

    // Leave class
    ApiResponse leaveClassroom(Long id);

    // Invite member
    ApiResponse inviteMembers(Long id, ClassMemberRequest classMemberRequest);

    //  Remove member
    ApiResponse removeMember(Long id, Long userId);

    // Get class detail
    ApiResponse getClassroomById(Long id);

    // Get class members
    ApiResponse getClassroomMembers(Long id);

    /** Lớp do user làm chủ hoặc đang tham gia */
    ApiResponse getMyClassrooms();
}
