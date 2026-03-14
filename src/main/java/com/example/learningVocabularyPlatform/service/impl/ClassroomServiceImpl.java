package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.ClassMemberRequest;
import com.example.learningVocabularyPlatform.dto.request.ClassroomRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.ClassMemberResponse;
import com.example.learningVocabularyPlatform.dto.response.ClassroomResponse;
import com.example.learningVocabularyPlatform.entity.ClassMemberEntity;
import com.example.learningVocabularyPlatform.entity.ClassroomEntity;
import com.example.learningVocabularyPlatform.entity.UserEntity;
import com.example.learningVocabularyPlatform.exception.ResourceNotFoundException;
import com.example.learningVocabularyPlatform.repository.ClassMemberRepository;
import com.example.learningVocabularyPlatform.repository.ClassroomRepository;
import com.example.learningVocabularyPlatform.repository.UserRepository;
import com.example.learningVocabularyPlatform.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ClassMemberRepository classMemberRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse createClassroom(ClassroomRequest req) {
        ClassroomEntity classroom = ClassroomEntity.builder()
                .name(req.getName())
                .description(req.getDescription())
                .build();

        classroomRepository.save(classroom);

        ClassroomResponse classroomResponse = ClassroomResponse.builder()
                .id(classroom.getId())
                .name(classroom.getName())
                .description(classroom.getDescription())
                .createdAt(classroom.getCreatedAt())
                .build();

        return ApiResponse.builder()
                .message("Tạo lớp học thành công!")
                .data(classroomResponse)
                .build();
    }

    @Override
    public ApiResponse updateClassroom(Long id, ClassroomRequest req) {
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));

        classroom.setName(req.getName());
        classroom.setDescription(req.getDescription());
        classroomRepository.save(classroom);

        ClassroomResponse classroomResponse = ClassroomResponse.builder()
                .id(classroom.getId())
                .name(classroom.getName())
                .description(classroom.getDescription())
                .createdAt(classroom.getCreatedAt())
                .build();

        return ApiResponse.builder()
                .message("Cập nhật lớp học thành công!")
                .data(classroomResponse)
                .build();
    }

    @Override
    public ApiResponse deleteClassroom(Long id) {
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));

        classroomRepository.delete(classroom);
        return ApiResponse.builder()
                .message("Xóa lớp học thành công!")
                .build();
    }

    @Override
    public ApiResponse joinClassroom(Long id) {
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));

        // JWT !!!!!!!!
        ClassMemberEntity classMember = ClassMemberEntity.builder()
                .classroom(classroom)
                .role("STUDENT")
                .build();
        classMemberRepository.save(classMember);

        return ApiResponse.builder()
                .message("Tham gia lớp học thành công!")
                .build();
    }

    @Override
    public ApiResponse leaveClassroom(Long id) {
        // JWT !!!!!!!!!!!!!
        return ApiResponse.builder()
                .message("Rời lớp học thành công!")
                .build();
    }

    @Override
    public ApiResponse inviteMembers(Long id, ClassMemberRequest req) {
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        UserEntity user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));
        ClassMemberEntity classMember = ClassMemberEntity.builder()
                .user(user)
                .classroom(classroom)
                .role("STUDENT")
                .build();

        classMemberRepository.save(classMember);
        return ApiResponse.builder()
                .message("Đã gửi lời mời thành công!")
                .build();
    }

    @Override
    public ApiResponse removeMember(Long id, Long userId) {
        classMemberRepository.deleteByClassroomIdAndUserId(id, userId);
        return ApiResponse.builder()
                .message("Xóa thành viên thành công!")
                .build();
    }

    @Override
    public ApiResponse getClassroomById(Long id) {
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));

        ClassroomResponse classroomResponse = ClassroomResponse.builder()
                .id(classroom.getId())
                .name(classroom.getName())
                .description(classroom.getDescription())
                .createdAt(classroom.getCreatedAt())
                .build();

        return ApiResponse.builder()
                .message("Thành công!")
                .data(classroomResponse)
                .build();
    }

    @Override
    public ApiResponse getClassroomMembers(Long id) {
        List<ClassMemberEntity> classMembers = classMemberRepository.findByClassroomId(id);

        List<ClassMemberResponse> classMemberResponses = classMembers.stream().map(member ->
                ClassMemberResponse.builder()
                        .id(member.getId())
                        .classId(id)
                        .userId(member.getUser().getId())
                        .role(member.getRole())
                        .joinedAt(member.getJoinedAt())
                        .build()
                ).toList();
        return ApiResponse.builder()
                .message("Lấy dữ liệu thành công!")
                .data(classMemberResponses)
                .build();
    }
}
