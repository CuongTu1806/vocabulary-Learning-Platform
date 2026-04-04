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

    // Hardcode user tạm thời, sau này lấy từ JWT
    private static final Long HARDCODE_USER_ID = 1L;

    @Override
    public ApiResponse createClassroom(ClassroomRequest req) {
        UserEntity owner = userRepository.findById(HARDCODE_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user!"));

        ClassroomEntity classroom = ClassroomEntity.builder()
                .name(req.getName())
                .description(req.getDescription())
                .owner(owner)
                .build();
        classroomRepository.save(classroom);

        ClassroomResponse response = toResponse(classroom);
        return ApiResponse.builder()
                .message("Tạo lớp học thành công!")
                .data(response)
                .build();
    }

    @Override
    public ApiResponse updateClassroom(Long id, ClassroomRequest req) {
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));

        classroom.setName(req.getName());
        classroom.setDescription(req.getDescription());
        classroomRepository.save(classroom);

        return ApiResponse.builder()
                .message("Cập nhật lớp học thành công!")
                .data(toResponse(classroom))
                .build();
    }

    @Override
    public ApiResponse deleteClassroom(Long id) {
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));

        classMemberRepository.deleteByClassroomId(id);
        classroomRepository.delete(classroom);
        return ApiResponse.builder()
                .message("Xóa lớp học thành công!")
                .build();
    }

    @Override
    public ApiResponse getClassroomById(Long id) {
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));

        return ApiResponse.builder()
                .message("Thành công!")
                .data(toResponse(classroom))
                .build();
    }

    @Override
    public ApiResponse getClassroomMembers(Long id) {
        List<ClassMemberEntity> classMembers = classMemberRepository.findByClassroomId(id);

        List<ClassMemberResponse> response = classMembers.stream()
                .map(member -> ClassMemberResponse.builder()
                        .id(member.getId())
                        .classId(id)
                        .userId(member.getUser().getId())
                        .role(member.getRole())
                        .joinedAt(member.getJoinedAt())
                        .build())
                .toList();

        return ApiResponse.builder()
                .message("Lấy dữ liệu thành công!")
                .data(response)
                .build();
    }

    @Override
    public ApiResponse joinClassroom(Long id) {
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));

        UserEntity user = userRepository.findById(HARDCODE_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user!"));

        // Kiểm tra đã join chưa
        boolean alreadyJoined = classMemberRepository.existsByClassroomIdAndUserId(id, HARDCODE_USER_ID);
        if (alreadyJoined) {
            return ApiResponse.builder()
                    .message("Bạn đã tham gia lớp học này rồi!")
                    .build();
        }

        ClassMemberEntity member = ClassMemberEntity.builder()
                .classroom(classroom)
                .user(user)
                .role("STUDENT")
                .build();
        classMemberRepository.save(member);

        return ApiResponse.builder()
                .message("Tham gia lớp học thành công!")
                .build();
    }

    @Override
    public ApiResponse leaveClassroom(Long id) {
        boolean isMember = classMemberRepository.existsByClassroomIdAndUserId(id, HARDCODE_USER_ID);
        if (!isMember) {
            return ApiResponse.builder()
                    .message("Bạn không phải thành viên của lớp học này!")
                    .build();
        }

        classMemberRepository.deleteByClassroomIdAndUserId(id, HARDCODE_USER_ID);
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

        // Kiểm tra đã là member chưa
        boolean alreadyMember = classMemberRepository.existsByClassroomIdAndUserId(id, req.getUserId());
        if (alreadyMember) {
            return ApiResponse.builder()
                    .message("Người dùng đã là thành viên của lớp học này rồi!")
                    .build();
        }

        ClassMemberEntity member = ClassMemberEntity.builder()
                .classroom(classroom)
                .user(user)
                .role("STUDENT")
                .build();
        classMemberRepository.save(member);

        return ApiResponse.builder()
                .message("Đã thêm thành viên thành công!")
                .build();
    }

    @Override
    public ApiResponse removeMember(Long id, Long userId) {
        boolean isMember = classMemberRepository.existsByClassroomIdAndUserId(id, userId);
        if (!isMember) {
            return ApiResponse.builder()
                    .message("Không tìm thấy thành viên trong lớp học!")
                    .build();
        }

        classMemberRepository.deleteByClassroomIdAndUserId(id, userId);
        return ApiResponse.builder()
                .message("Xóa thành viên thành công!")
                .build();
    }

    // Helper method - convert Entity sang DTO
    private ClassroomResponse toResponse(ClassroomEntity classroom) {
        return ClassroomResponse.builder()
                .id(classroom.getId())
                .name(classroom.getName())
                .description(classroom.getDescription())
                .ownerId(classroom.getOwner() != null ? classroom.getOwner().getId() : null)
                .createdAt(classroom.getCreatedAt())
                .build();
    }
}
