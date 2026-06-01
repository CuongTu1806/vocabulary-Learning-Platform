package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.ClassBoardCommentRequest;
import com.example.learningVocabularyPlatform.dto.request.ClassBoardPostRequest;
import com.example.learningVocabularyPlatform.dto.request.ClassMemberRequest;
import com.example.learningVocabularyPlatform.dto.request.ClassroomRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.ClassBoardCommentResponse;
import com.example.learningVocabularyPlatform.dto.response.ClassBoardPostResponse;
import com.example.learningVocabularyPlatform.dto.response.ClassMemberResponse;
import com.example.learningVocabularyPlatform.dto.response.ClassroomResponse;
import com.example.learningVocabularyPlatform.entity.ClassBoardCommentEntity;
import com.example.learningVocabularyPlatform.entity.ClassBoardPostEntity;
import com.example.learningVocabularyPlatform.entity.ClassMemberEntity;
import com.example.learningVocabularyPlatform.entity.ClassroomEntity;
import com.example.learningVocabularyPlatform.entity.UserEntity;
import com.example.learningVocabularyPlatform.exception.ResourceNotFoundException;
import com.example.learningVocabularyPlatform.repository.ClassBoardCommentRepository;
import com.example.learningVocabularyPlatform.repository.ClassBoardPostRepository;
import com.example.learningVocabularyPlatform.repository.ClassMemberRepository;
import com.example.learningVocabularyPlatform.repository.ClassroomRepository;
import com.example.learningVocabularyPlatform.repository.UserRepository;
import com.example.learningVocabularyPlatform.service.AuthenticatedUserService;
import com.example.learningVocabularyPlatform.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ClassMemberRepository classMemberRepository;
        private final ClassBoardPostRepository classBoardPostRepository;
        private final ClassBoardCommentRepository classBoardCommentRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserService authenticatedUserService;

    private static boolean isClassOwner(ClassroomEntity classroom, UserEntity user) {
        return classroom.getOwner() != null && classroom.getOwner().getId().equals(user.getId());
    }

    private boolean canAccessClassroom(ClassroomEntity classroom, UserEntity user) {
        if (isClassOwner(classroom, user)) {
            return true;
        }
                return classMemberRepository.existsByClassroomIdAndUserIdAndApprovedTrue(classroom.getId(), user.getId());
    }

        @Override
        public ApiResponse createClassroom(Long currentUserId, ClassroomRequest req) {
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

        List<ClassMemberEntity> classMembers = classMemberRepository.findByClassroomIdAndApprovedTrue(id);

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
    public ApiResponse getPendingJoinRequests(Long id) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        if (!isClassOwner(classroom, current)) {
            return ApiResponse.builder().message("Chỉ chủ lớp mới xem được danh sách yêu cầu").build();
        }
        List<ClassMemberEntity> pending = classMemberRepository.findByClassroomIdAndApprovedFalse(id);
        List<ClassMemberResponse> response = pending.stream()
                .map(member -> ClassMemberResponse.builder()
                        .id(member.getId())
                        .classId(id)
                        .userId(member.getUser().getId())
                        .role(member.getRole())
                        .joinedAt(member.getJoinedAt())
                        .build())
                .toList();
        return ApiResponse.builder().message("OK").data(response).build();
        }

    @Override
    public ApiResponse approveMember(Long classId, Long userId) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ClassroomEntity classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        if (!isClassOwner(classroom, current)) {
            return ApiResponse.builder().message("Chỉ chủ lớp mới có quyền thực hiện").build();
        }
        List<ClassMemberEntity> pending = classMemberRepository.findByClassroomIdAndApprovedFalse(classId);
        ClassMemberEntity target = pending.stream()
                .filter(m -> m.getUser() != null && m.getUser().getId().equals(userId))
                .findFirst()
                .orElse(null);
        if (target == null) {
            return ApiResponse.builder().message("Không tìm thấy yêu cầu tham gia").build();
        }
        target.setApproved(true);
        target.setJoinedAt(java.time.LocalDateTime.now());
        classMemberRepository.save(target);
        return ApiResponse.builder().message("Duyệt thành viên thành công").build();
        }

    @Override
    public ApiResponse rejectMember(Long classId, Long userId) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ClassroomEntity classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        if (!isClassOwner(classroom, current)) {
            return ApiResponse.builder().message("Chỉ chủ lớp mới có quyền thực hiện").build();
        }
        List<ClassMemberEntity> pending = classMemberRepository.findByClassroomIdAndApprovedFalse(classId);
        ClassMemberEntity target = pending.stream()
                .filter(m -> m.getUser() != null && m.getUser().getId().equals(userId))
                .findFirst()
                .orElse(null);
        if (target == null) {
            return ApiResponse.builder().message("Không tìm thấy yêu cầu tham gia").build();
        }
        classMemberRepository.delete(target);
        return ApiResponse.builder().message("Từ chối yêu cầu tham gia").build();
        }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getClassBoard(Long classId) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ClassroomEntity classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        if (!canAccessClassroom(classroom, current)) {
            return ApiResponse.builder()
                    .message("Bạn không có quyền xem bảng tin của lớp này")
                    .build();
        }

        List<ClassBoardPostResponse> response = classBoardPostRepository.findByClassroom_IdOrderByCreatedAtDesc(classId)
                .stream()
                .map(this::toBoardPostResponse)
                .toList();
        return ApiResponse.builder().message("OK").data(response).build();
    }

    @Override
    public ApiResponse createClassBoardPost(Long classId, ClassBoardPostRequest request) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ClassroomEntity classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        if (!isClassOwner(classroom, current)) {
            return ApiResponse.builder().message("Chỉ chủ lớp mới được tạo thông báo").build();
        }

        String content = request != null && request.getContent() != null ? request.getContent().trim() : "";
        if (content.isBlank()) {
            return ApiResponse.builder().message("Nội dung thông báo không được để trống").build();
        }

        ClassBoardPostEntity post = ClassBoardPostEntity.builder()
                .classroom(classroom)
                .author(current)
                .content(content)
                .build();
        classBoardPostRepository.save(post);

        return ApiResponse.builder()
                .message("Đăng thông báo thành công")
                .data(toBoardPostResponse(post))
                .build();
    }

    @Override
    public ApiResponse addClassBoardComment(Long classId, Long postId, ClassBoardCommentRequest request) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ClassroomEntity classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));
        if (!canAccessClassroom(classroom, current)) {
            return ApiResponse.builder().message("Bạn không có quyền bình luận trên bảng tin").build();
        }

        ClassBoardPostEntity post = classBoardPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng"));
        if (post.getClassroom() == null || !post.getClassroom().getId().equals(classId)) {
            return ApiResponse.builder().message("Bài đăng không thuộc lớp học này").build();
        }

        String content = request != null && request.getContent() != null ? request.getContent().trim() : "";
        if (content.isBlank()) {
            return ApiResponse.builder().message("Nội dung bình luận không được để trống").build();
        }

        ClassBoardCommentEntity comment = ClassBoardCommentEntity.builder()
                .post(post)
                .author(current)
                .content(content)
                .build();
        classBoardCommentRepository.save(comment);

        return ApiResponse.builder()
                .message("Đã thêm bình luận")
                .data(toBoardCommentResponse(comment))
                .build();
    }

    @Override
        public ApiResponse joinClassroom(Long id, Long currentUserId) {
        ClassroomEntity classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học!"));

        UserEntity user = authenticatedUserService.requireCurrentUser();

        // Kiểm tra đã có request hoặc đã join chưa
        boolean alreadyMember = classMemberRepository.existsByClassroomIdAndUserIdAndApprovedTrue(id, user.getId());
        if (alreadyMember) {
            return ApiResponse.builder()
                    .message("Bạn đã tham gia lớp học này rồi!")
                    .build();
        }
        boolean alreadyRequested = classMemberRepository.findByClassroomIdAndApprovedFalse(id).stream()
                .anyMatch(m -> m.getUser() != null && m.getUser().getId().equals(user.getId()));
        if (alreadyRequested) {
            return ApiResponse.builder().message("Bạn đã gửi yêu cầu tham gia. Vui lòng chờ duyệt.").build();
        }

        ClassMemberEntity member = ClassMemberEntity.builder()
                .classroom(classroom)
                .user(user)
                .role("STUDENT")
                .approved(false)
                .build();
        classMemberRepository.save(member);

        return ApiResponse.builder()
                .message("Yêu cầu tham gia đã được gửi. Vui lòng chờ duyệt.")
                .build();
    }

        @Override
        public ApiResponse leaveClassroom(Long id, Long currentUserId) {
        UserEntity user = authenticatedUserService.requireCurrentUser();
                boolean isMember = classMemberRepository.existsByClassroomIdAndUserIdAndApprovedTrue(id, user.getId());
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
        boolean alreadyMember = classMemberRepository.existsByClassroomIdAndUserIdAndApprovedTrue(id, req.getUserId());
        if (alreadyMember) {
            return ApiResponse.builder()
                    .message("Người dùng đã là thành viên của lớp học này rồi!")
                    .build();
        }

        ClassMemberEntity member = ClassMemberEntity.builder()
                .classroom(classroom)
                .user(user)
                .role("STUDENT")
                .approved(true)
                .joinedAt(java.time.LocalDateTime.now())
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

        boolean isMember = classMemberRepository.existsByClassroomIdAndUserIdAndApprovedTrue(id, userId);
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

        private ClassBoardPostResponse toBoardPostResponse(ClassBoardPostEntity post) {
                List<ClassBoardCommentResponse> comments = classBoardCommentRepository.findByPost_IdOrderByCreatedAtAsc(post.getId())
                                .stream()
                                .map(this::toBoardCommentResponse)
                                .toList();
                return ClassBoardPostResponse.builder()
                                .id(post.getId())
                                .classroomId(post.getClassroom() != null ? post.getClassroom().getId() : null)
                                .authorId(post.getAuthor() != null ? post.getAuthor().getId() : null)
                                .authorName(post.getAuthor() != null && post.getAuthor().getUsername() != null ? post.getAuthor().getUsername() : "Người dùng")
                                .content(post.getContent())
                                .createdAt(post.getCreatedAt())
                                .comments(comments)
                                .commentCount(comments.size())
                                .build();
        }

        private ClassBoardCommentResponse toBoardCommentResponse(ClassBoardCommentEntity comment) {
                return ClassBoardCommentResponse.builder()
                                .id(comment.getId())
                                .authorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null)
                                .authorName(comment.getAuthor() != null && comment.getAuthor().getUsername() != null ? comment.getAuthor().getUsername() : "Người dùng")
                                .content(comment.getContent())
                                .createdAt(comment.getCreatedAt())
                                .build();
        }
}
