package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.config.StorageProperties;
import com.example.learningVocabularyPlatform.dto.request.AssignmentRequest;
import com.example.learningVocabularyPlatform.dto.request.AssignmentSubmissionRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.AssignmentAttachmentResponse;
import com.example.learningVocabularyPlatform.dto.response.AssignmentResponse;
import com.example.learningVocabularyPlatform.dto.response.AssignmentSubmissionResponse;
import com.example.learningVocabularyPlatform.dto.response.FileDownloadDto;
import com.example.learningVocabularyPlatform.dto.response.SubmissionAttachmentResponse;
import com.example.learningVocabularyPlatform.entity.AssignmentAttachmentEntity;
import com.example.learningVocabularyPlatform.entity.AssignmentEntity;
import com.example.learningVocabularyPlatform.entity.AssignmentSubmissionEntity;
import com.example.learningVocabularyPlatform.entity.ClassroomEntity;
import com.example.learningVocabularyPlatform.entity.SubmissionAttachmentEntity;
import com.example.learningVocabularyPlatform.entity.UserEntity;
import com.example.learningVocabularyPlatform.exception.ResourceNotFoundException;
import com.example.learningVocabularyPlatform.repository.*;
import com.example.learningVocabularyPlatform.service.AssignmentService;
import com.example.learningVocabularyPlatform.service.AuthenticatedUserService;
import com.example.learningVocabularyPlatform.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final AssignmentAttachmentRepository assignmentAttachmentRepository;
    private final SubmissionAttachmentRepository submissionAttachmentRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassMemberRepository classMemberRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final FileStorageService fileStorageService;
    private final StorageProperties storageProperties;

    private static boolean isClassOwner(ClassroomEntity classroom, UserEntity user) {
        return classroom.getOwner() != null && classroom.getOwner().getId().equals(user.getId());
    }

    /** Người tạo bài hoặc chủ lớp (chấm điểm / xem submission). */
    private boolean canManageAssignment(AssignmentEntity assignment, UserEntity user) {
        if (assignment.getUserCreated() != null && assignment.getUserCreated().getId().equals(user.getId())) {
            return true;
        }
        ClassroomEntity c = assignment.getClassroom();
        return c != null && isClassOwner(c, user);
    }

    private boolean canViewAssignment(AssignmentEntity assignment, UserEntity user) {
        if (canManageAssignment(assignment, user)) {
            return true;
        }
        ClassroomEntity c = assignment.getClassroom();
        if (c == null) {
            return false;
        }
        return classMemberRepository.existsByClassroomIdAndUserId(c.getId(), user.getId());
    }

    @Override
    @Transactional
        public ApiResponse createAssignment(AssignmentRequest req, Long currentUserId) {
        ClassroomEntity classroom = classroomRepository.findById(req.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học"));
        UserEntity user = authenticatedUserService.requireCurrentUser();
        if (!isClassOwner(classroom, user)) {
            return ApiResponse.builder()
                    .message("Chỉ chủ lớp mới được tạo bài tập")
                    .build();
        }

        AssignmentEntity assignmentEntity = AssignmentEntity.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .dueDate(req.getDueDate())
                .classroom(classroom)
                .userCreated(user)
                .build();

        assignmentRepository.save(assignmentEntity);

        return ApiResponse.builder()
                .message("Tạo bài tập thành công")
                .data(toAssignmentResponse(assignmentEntity))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse updateAssignment(AssignmentRequest req, Long id) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        AssignmentEntity assignmentEntity = assignmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));
        if (!canManageAssignment(assignmentEntity, current)) {
            return ApiResponse.builder()
                    .message("Bạn không có quyền cập nhật bài tập này")
                    .build();
        }

        assignmentEntity.setTitle(req.getTitle());
        assignmentEntity.setDescription(req.getDescription());
        assignmentEntity.setDueDate(req.getDueDate());
        if (req.getClassId() != null) {
            ClassroomEntity classroomEntity = classroomRepository.findById(req.getClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học"));
            assignmentEntity.setClassroom(classroomEntity);
        }

        assignmentRepository.save(assignmentEntity);

        return ApiResponse.builder()
                .message("Cập nhật bài kiểm tra thành công")
                .data(toAssignmentResponse(assignmentEntity))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse deleteAssignment(Long id) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        AssignmentEntity assignmentEntity = assignmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));
        if (!canManageAssignment(assignmentEntity, current)) {
            return ApiResponse.builder()
                    .message("Bạn không có quyền xóa bài tập này")
                    .build();
        }

        List<AssignmentSubmissionEntity> submissions = assignmentSubmissionRepository.findByAssignment_Id(id);
        for (AssignmentSubmissionEntity s : submissions) {
            List<SubmissionAttachmentEntity> subs = submissionAttachmentRepository.findBySubmission_Id(s.getId());
            for (SubmissionAttachmentEntity a : subs) {
                fileStorageService.deleteIfExists(a.getRelativePath());
            }
            submissionAttachmentRepository.deleteAll(subs);
        }
        assignmentSubmissionRepository.deleteAll(submissions);

        List<AssignmentAttachmentEntity> assAttachments = assignmentAttachmentRepository.findByAssignment_Id(id);
        for (AssignmentAttachmentEntity a : assAttachments) {
            fileStorageService.deleteIfExists(a.getRelativePath());
        }
        assignmentAttachmentRepository.deleteAll(assAttachments);

        assignmentRepository.delete(assignmentEntity);
        return ApiResponse.builder()
                .message("Xóa bài kiểm tra thành công")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse getAssignments(Long classId) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        List<AssignmentEntity> list;
        if (classId != null) {
            ClassroomEntity classroom = classroomRepository.findById(classId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học"));
            if (!isClassOwner(classroom, current)
                    && !classMemberRepository.existsByClassroomIdAndUserId(classId, current.getId())) {
                return ApiResponse.builder()
                        .message("Bạn không có quyền xem bài tập của lớp này")
                        .build();
            }
            list = assignmentRepository.findByClassroom_Id(classId);
        } else {
            list = assignmentRepository.findByUserCreated_Id(current.getId());
        }

        List<AssignmentResponse> responseList = list.stream().map(this::toAssignmentResponse).toList();

        return ApiResponse.builder()
                .message("Lấy danh sách bài kiểm tra thành công")
                .data(responseList)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse getAssignmentById(Long id) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        AssignmentEntity assignmentEntity = assignmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));
        if (!canViewAssignment(assignmentEntity, current)) {
            return ApiResponse.builder()
                    .message("Bạn không có quyền xem bài tập này")
                    .build();
        }

        List<AssignmentAttachmentEntity> att = assignmentAttachmentRepository.findByAssignment_Id(id);
        AssignmentResponse dto = toAssignmentResponse(assignmentEntity, att);
        boolean manage = canManageAssignment(assignmentEntity, current);
        dto.setCurrentUserCanGrade(manage);
        if (!manage && assignmentEntity.getClassroom() != null
                && classMemberRepository.existsByClassroomIdAndUserId(assignmentEntity.getClassroom().getId(), current.getId())) {
            dto.setCurrentUserHasSubmitted(
                    assignmentSubmissionRepository
                            .findByAssignment_IdAndUser_Id(assignmentEntity.getId(), current.getId())
                            .isPresent());
        }

        return ApiResponse.builder()
                .message("OK")
                .data(dto)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse submitAssignment(Long assignmentId, AssignmentSubmissionRequest req) {
        if (req.getContent() == null || req.getContent().isBlank()) {
            return ApiResponse.builder()
                    .message("Nhập nội dung bài làm")
                    .build();
        }
        AssignmentEntity assignmentEntity = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));

        if (assignmentEntity.getDueDate() != null && LocalDateTime.now().isAfter(assignmentEntity.getDueDate())) {
            return ApiResponse.builder()
                    .message("Đã quá hạn nộp bài")
                    .build();
        }

        UserEntity userEntity = authenticatedUserService.requireCurrentUser();

        boolean isMember = classMemberRepository.existsByClassroomIdAndUserId(assignmentEntity.getClassroom().getId(), userEntity.getId());
        if (!isMember) {
            return ApiResponse.builder().message("Bạn không phải thành viên của lớp học").build();
        }

        if (assignmentSubmissionRepository.findByAssignment_IdAndUser_Id(assignmentId, userEntity.getId()).isPresent()) {
            return ApiResponse.builder()
                    .message("Bạn đã nộp bài rồi")
                    .build();
        }

        AssignmentSubmissionEntity submissionEntity = AssignmentSubmissionEntity.builder()
                .content(req.getContent().trim())
                .score(0f)
                .submittedAt(LocalDateTime.now())
                .assignment(assignmentEntity)
                .user(userEntity)
                .build();
        assignmentSubmissionRepository.save(submissionEntity);

        return ApiResponse.builder()
                .message("Nộp bài thành công")
                .data(toAssignmentSubmissionResponse(submissionEntity))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse submitAssignmentMultipart(Long assignmentId, String content, MultipartFile[] files) {
        AssignmentEntity assignmentEntity = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));

        if (assignmentEntity.getDueDate() != null && LocalDateTime.now().isAfter(assignmentEntity.getDueDate())) {
            return ApiResponse.builder()
                    .message("Đã quá hạn nộp bài")
                    .build();
        }

        UserEntity userEntity = authenticatedUserService.requireCurrentUser();

        boolean isMember = classMemberRepository.existsByClassroomIdAndUserId(assignmentEntity.getClassroom().getId(), userEntity.getId());
        if (!isMember) {
            return ApiResponse.builder().message("Bạn không phải thành viên của lớp học").build();
        }

        if (assignmentSubmissionRepository.findByAssignment_IdAndUser_Id(assignmentId, userEntity.getId()).isPresent()) {
            return ApiResponse.builder()
                    .message("Bạn đã nộp bài rồi")
                    .build();
        }

        int n = files == null ? 0 : (int) java.util.Arrays.stream(files).filter(f -> f != null && !f.isEmpty()).count();
        String text = content != null ? content.trim() : "";
        if (text.isEmpty() && n == 0) {
            return ApiResponse.builder()
                    .message("Nhập nội dung hoặc đính kèm ít nhất một file")
                    .build();
        }
        if (n > storageProperties.getMaxFilesPerRequest()) {
            return ApiResponse.builder()
                    .message("Quá số file cho phép mỗi lần nộp")
                    .build();
        }

        AssignmentSubmissionEntity submissionEntity = AssignmentSubmissionEntity.builder()
                .content(text.isEmpty() ? null : text)
                .score(0f)
                .submittedAt(LocalDateTime.now())
                .assignment(assignmentEntity)
                .user(userEntity)
                .build();
        assignmentSubmissionRepository.save(submissionEntity);

        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                try {
                    String rel = fileStorageService.store(file, "submission/" + submissionEntity.getId());
                    SubmissionAttachmentEntity att = SubmissionAttachmentEntity.builder()
                            .submission(submissionEntity)
                            .relativePath(rel)
                            .originalFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file")
                            .contentType(file.getContentType())
                            .sizeBytes(file.getSize())
                            .build();
                    submissionAttachmentRepository.save(att);
                } catch (IllegalArgumentException e) {
                    return ApiResponse.builder().message(e.getMessage()).build();
                } catch (IOException e) {
                    return ApiResponse.builder().message("Lỗi lưu file nộp bài").build();
                }
            }
        }

        AssignmentSubmissionEntity reloaded = assignmentSubmissionRepository.findByIdWithAttachments(submissionEntity.getId())
                .orElse(submissionEntity);

        return ApiResponse.builder()
                .message("Nộp bài thành công")
                .data(toAssignmentSubmissionResponse(reloaded))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse uploadAssignmentAttachments(Long assignmentId, MultipartFile[] files) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        AssignmentEntity assignment = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));
        if (!canManageAssignment(assignment, current)) {
            return ApiResponse.builder()
                    .message("Bạn không có quyền tải file lên bài tập này")
                    .build();
        }
        if (files == null || files.length == 0) {
            return ApiResponse.builder().message("Chọn ít nhất một file").build();
        }
        long count = java.util.Arrays.stream(files).filter(f -> f != null && !f.isEmpty()).count();
        if (count == 0) {
            return ApiResponse.builder().message("Chọn ít nhất một file").build();
        }
        if (count > storageProperties.getMaxFilesPerRequest()) {
            return ApiResponse.builder().message("Quá số file cho phép mỗi lần upload").build();
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            try {
                String rel = fileStorageService.store(file, "assignment/" + assignmentId);
                AssignmentAttachmentEntity att = AssignmentAttachmentEntity.builder()
                        .assignment(assignment)
                        .relativePath(rel)
                        .originalFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file")
                        .contentType(file.getContentType())
                        .sizeBytes(file.getSize())
                        .build();
                assignmentAttachmentRepository.save(att);
            } catch (IllegalArgumentException e) {
                return ApiResponse.builder().message(e.getMessage()).build();
            } catch (IOException e) {
                return ApiResponse.builder().message("Lỗi lưu file đính kèm").build();
            }
        }

        List<AssignmentAttachmentEntity> list = assignmentAttachmentRepository.findByAssignment_Id(assignmentId);
        return ApiResponse.builder()
                .message("Upload file thành công")
                .data(list.stream().map(this::toAssignmentAttachmentResponse).toList())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse deleteAssignmentAttachment(Long assignmentId, Long attachmentId) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        AssignmentEntity assignment = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));
        if (!canManageAssignment(assignment, current)) {
            return ApiResponse.builder()
                    .message("Bạn không có quyền xóa file này")
                    .build();
        }
        AssignmentAttachmentEntity att = assignmentAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy file"));
        if (att.getAssignment() == null || !att.getAssignment().getId().equals(assignmentId)) {
            return ApiResponse.builder().message("File không thuộc bài tập này").build();
        }
        fileStorageService.deleteIfExists(att.getRelativePath());
        assignmentAttachmentRepository.delete(att);
        return ApiResponse.builder().message("Đã xóa file").build();
    }

    @Override
    @Transactional
    public ApiResponse getSubmissions(Long assignmentId) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        AssignmentEntity assignmentEntity = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));
        if (!canManageAssignment(assignmentEntity, current)) {
            return ApiResponse.builder()
                    .message("Chỉ giáo viên chủ lớp hoặc người tạo bài mới xem được danh sách nộp bài")
                    .build();
        }

        List<AssignmentSubmissionEntity> list = assignmentSubmissionRepository.findByAssignmentIdWithAttachments(assignmentId);
        List<AssignmentSubmissionResponse> responseList = list.stream()
                .map(this::toAssignmentSubmissionResponse)
                .toList();

        return ApiResponse.builder()
                .message("Lấy danh sách nộp bài thành công")
                .data(responseList)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse gradeAssignment(Long assignmentId, Long submissionId, Float score) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        AssignmentSubmissionEntity submissionEntity = assignmentSubmissionRepository
                .findByIdWithAssignmentDetails(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài nộp không tồn tại"));
        if (!submissionEntity.getAssignment().getId().equals(assignmentId)) {
            return ApiResponse.builder().message("Bài nộp không thuộc bài kiểm tra này").build();
        }
        if (!canManageAssignment(submissionEntity.getAssignment(), current)) {
            return ApiResponse.builder()
                    .message("Bạn không có quyền chấm điểm bài tập này")
                    .build();
        }

        submissionEntity.setScore(score);
        assignmentSubmissionRepository.save(submissionEntity);

        AssignmentSubmissionEntity reloaded = assignmentSubmissionRepository.findByIdWithAttachments(submissionId)
                .orElse(submissionEntity);

        return ApiResponse.builder()
                .message("Chấm điểm hoàn tất")
                .data(toAssignmentSubmissionResponse(reloaded))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownloadDto downloadAssignmentAttachment(Long attachmentId) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        AssignmentAttachmentEntity att = assignmentAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy file"));
        AssignmentEntity assignment = att.getAssignment();
        if (!canViewAssignment(assignment, current)) {
            throw new ResourceNotFoundException("Bạn không có quyền tải file này");
        }
        var resource = fileStorageService.loadAsResource(att.getRelativePath());
        String ct = att.getContentType() != null && !att.getContentType().isBlank()
                ? att.getContentType()
                : "application/octet-stream";
        return new FileDownloadDto(resource, att.getOriginalFilename(), ct);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownloadDto downloadSubmissionAttachment(Long submissionId, Long attachmentId) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        SubmissionAttachmentEntity att = submissionAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy file"));
        if (att.getSubmission() == null || !att.getSubmission().getId().equals(submissionId)) {
            throw new ResourceNotFoundException("File không thuộc bài nộp này");
        }
        AssignmentSubmissionEntity sub = att.getSubmission();
        AssignmentEntity assignment = sub.getAssignment();
        boolean isSubmitter = sub.getUser() != null && sub.getUser().getId().equals(current.getId());
        if (!isSubmitter && !canManageAssignment(assignment, current)) {
            throw new ResourceNotFoundException("Bạn không có quyền tải file này");
        }
        var resource = fileStorageService.loadAsResource(att.getRelativePath());
        String ct = att.getContentType() != null && !att.getContentType().isBlank()
                ? att.getContentType()
                : "application/octet-stream";
        return new FileDownloadDto(resource, att.getOriginalFilename(), ct);
    }

    private AssignmentResponse toAssignmentResponse(AssignmentEntity entity) {
        return toAssignmentResponse(entity, null);
    }

    private AssignmentResponse toAssignmentResponse(AssignmentEntity entity, List<AssignmentAttachmentEntity> attachments) {
        AssignmentResponse.AssignmentResponseBuilder b = AssignmentResponse.builder()
                .id(entity.getId())
                .classId(entity.getClassroom() != null ? entity.getClassroom().getId() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .dueDate(entity.getDueDate())
                .createdByUserId(entity.getUserCreated() != null ? entity.getUserCreated().getId() : null);
        if (attachments != null) {
            b.attachments(attachments.stream().map(this::toAssignmentAttachmentResponse).toList());
        }
        return b.build();
    }

    private AssignmentAttachmentResponse toAssignmentAttachmentResponse(AssignmentAttachmentEntity e) {
        return AssignmentAttachmentResponse.builder()
                .id(e.getId())
                .originalFilename(e.getOriginalFilename())
                .contentType(e.getContentType())
                .sizeBytes(e.getSizeBytes())
                .build();
    }

    private AssignmentSubmissionResponse toAssignmentSubmissionResponse(AssignmentSubmissionEntity entity) {
        List<SubmissionAttachmentEntity> list = entity.getAttachments();
        if (list == null || list.isEmpty()) {
            list = submissionAttachmentRepository.findBySubmission_Id(entity.getId());
        }
        List<SubmissionAttachmentResponse> attResponses = list.stream()
                .map(a -> SubmissionAttachmentResponse.builder()
                        .id(a.getId())
                        .originalFilename(a.getOriginalFilename())
                        .contentType(a.getContentType())
                        .sizeBytes(a.getSizeBytes())
                        .build())
                .toList();

        return AssignmentSubmissionResponse.builder()
                .id(entity.getId())
                .assignmentId(entity.getAssignment().getId())
                .userId(entity.getUser().getId())
                .content(entity.getContent())
                .score(entity.getScore())
                .submittedAt(entity.getSubmittedAt())
                .attachments(attResponses)
                .build();
    }
}
