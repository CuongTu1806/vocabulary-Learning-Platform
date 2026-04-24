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
import com.example.learningVocabularyPlatform.service.AuthenticatedUserService;
import com.example.learningVocabularyPlatform.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ClassMemberRepository classMemberRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserService authenticatedUserService;

    private static boolean isClassOwner(ClassroomEntity classroom, UserEntity user) {
        return classroom.getOwner() != null && classroom.getOwner().getId().equals(user.getId());
    }

    private boolean canAccessClassroom(ClassroomEntity classroom, UserEntity user) {
        if (isClassOwner(classroom, user)) {
            return true;
        }
        return classMemberRepository.existsByClassroomIdAndUserId(classroom.getId(), user.getId());
    }

    @Override
    public ApiResponse createClassroom(ClassroomRequest req) {
        UserEntity owner = authenticatedUserService.requireCurrentUser();

        ClassroomEntity classroom = ClassroomEntity.builder()
                .name(req.getName())
                .description(req.getDescription())
                .owner(owner)
                .build();
        classroomRepository.save(classroom);

        ClassroomResponse response = toResponse(classroom, owner.getId());
        return ApiResponse.builder()
                .message("Tạo lớp học thành công!")
                .data(response)
                .build();
    }

    @Override
    public ApiResponse updateClassroom(Long id, ClassroomRequest req) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        if (!isClassOwner(classroom, current)) {
            return ApiResponse.builder()
                    .message("Chỉ chủ lớp mới được cập nhật lớp học")
                    .build();
        }

        classroom.setName(req.getName());
        classroom.setDescription(req.getDescription());
        classroomRepository.save(classroom);

        return ApiResponse.builder()
                .message("Cập nhật lớp học thành công!")
                .data(toResponse(classroom, current.getId()))
                .build();
    }

    @Override
    public ApiResponse deleteClassroom(Long id) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        if (!isClassOwner(classroom, current)) {
            return ApiResponse.builder()
                    .message("Chỉ chủ lớp mới được xóa lớp học")
                    .build();
        }

        classMemberRepository.deleteByClassroomId(id);
        classroomRepository.delete(classroom);
        return ApiResponse.builder()
                .message("Xóa lớp học thành công!")
                .build();
    }

    @Override
    public ApiResponse getClassroomById(Long id) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        if (!canAccessClassroom(classroom, current)) {
            return ApiResponse.builder()
                    .message("Bạn không có quyền xem lớp học này")
                    .build();
        }

        return ApiResponse.builder()
                .message("Thành công!")
                .data(toResponse(classroom, current.getId()))
                .build();
    }

    @Override
    public ApiResponse getMyClassrooms() {
        UserEntity me = authenticatedUserService.requireCurrentUser();
        Long uid = me.getId();
        Map<Long, ClassroomEntity> map = new LinkedHashMap<>();
        for (ClassroomEntity c : classroomRepository.findByOwnerId(uid)) {
            map.putIfAbsent(c.getId(), c);
        }
        for (ClassMemberEntity m : classMemberRepository.findByUser_Id(uid)) {
            ClassroomEntity c = m.getClassroom();
            if (c != null) {
                map.putIfAbsent(c.getId(), c);
            }
        }
        List<ClassroomResponse> list = map.values().stream()
                .sorted(Comparator.comparing(ClassroomEntity::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(c -> toResponse(c, uid))
                .toList();
        return ApiResponse.builder()
                .message("OK")
                .data(list)
                .build();
    }

    @Override
    public ApiResponse getClassroomMembers(Long id) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        if (!canAccessClassroom(classroom, current)) {
            return ApiResponse.builder()
                    .message("Bạn không có quyền xem danh sách thành viên")
                    .build();
        }

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
        public ApiResponse joinClassroom(Long id, Long currentUserId) {
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));

        UserEntity user = authenticatedUserService.requireCurrentUser();

        // Kiểm tra đã join chưa
        boolean alreadyJoined = classMemberRepository.existsByClassroomIdAndUserId(id, user.getId());
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
        UserEntity user = authenticatedUserService.requireCurrentUser();
        boolean isMember = classMemberRepository.existsByClassroomIdAndUserId(id, user.getId());
        if (!isMember) {
            return ApiResponse.builder()
                    .message("Bạn không phải thành viên của lớp học này!")
                    .build();
        }

        classMemberRepository.deleteByClassroomIdAndUserId(id, user.getId());
        return ApiResponse.builder()
                .message("Rời lớp học thành công!")
                .build();
    }

    @Override
    public ApiResponse inviteMembers(Long id, ClassMemberRequest req) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        if (!isClassOwner(classroom, current)) {
            return ApiResponse.builder()
                    .message("Chỉ chủ lớp mới được mời thành viên")
                    .build();
        }

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
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        if (!isClassOwner(classroom, current)) {
            return ApiResponse.builder()
                    .message("Chỉ chủ lớp mới được xóa thành viên")
                    .build();
        }

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

    private ClassroomResponse toResponse(ClassroomEntity classroom, Long currentUserId) {
        ClassroomResponse.ClassroomResponseBuilder b = ClassroomResponse.builder()
                .id(classroom.getId())
                .name(classroom.getName())
                .description(classroom.getDescription())
                .ownerId(classroom.getOwner() != null ? classroom.getOwner().getId() : null)
                .createdAt(classroom.getCreatedAt());
        if (currentUserId != null && classroom.getOwner() != null) {
            b.currentUserIsOwner(classroom.getOwner().getId().equals(currentUserId));
        }
        return b.build();
    }
}
