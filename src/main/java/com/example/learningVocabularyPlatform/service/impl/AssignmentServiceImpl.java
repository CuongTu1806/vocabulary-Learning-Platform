package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.AssignmentRequest;
import com.example.learningVocabularyPlatform.dto.request.AssignmentSubmissionRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.AssignmentResponse;
import com.example.learningVocabularyPlatform.dto.response.AssignmentSubmissionResponse;
import com.example.learningVocabularyPlatform.entity.AssignmentEntity;
import com.example.learningVocabularyPlatform.entity.AssignmentSubmissionEntity;
import com.example.learningVocabularyPlatform.entity.ClassroomEntity;
import com.example.learningVocabularyPlatform.entity.UserEntity;
import com.example.learningVocabularyPlatform.exception.ResourceNotFoundException;
import com.example.learningVocabularyPlatform.repository.*;
import com.example.learningVocabularyPlatform.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ClassMemberRepository classMemberRepository;

    private static final Long HARDCODE_USER_ID = 1L;
    private static final Long HARDCODE_STUDENT_ID = 4L;

    @Override
    @Transactional
    public ApiResponse createAssignment(AssignmentRequest req) {
        ClassroomEntity classroom = classroomRepository.findById(req.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học"));
        UserEntity user = userRepository.findById(HARDCODE_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));


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
        AssignmentEntity assignmentEntity = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));

        assignmentEntity.setTitle(req.getTitle());
        assignmentEntity.setDescription(req.getDescription());
        assignmentEntity.setDueDate(req.getDueDate());
        if(req.getClassId() != null) {
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
        AssignmentEntity assignmentEntity = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));

        assignmentSubmissionRepository.deleteAll(assignmentSubmissionRepository.findByAssignment_Id(id));
        assignmentRepository.delete(assignmentEntity);
        return ApiResponse.builder()
                .message("Xóa bài kiểm tra thành công")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse getAssignments(Long classId) {
        List<AssignmentEntity> list = classId != null
                ? assignmentRepository.findByClassroom_Id(classId)
                : assignmentRepository.findAll();

        List<AssignmentResponse> responseList = list.stream().map(this::toAssignmentResponse).toList();

        return ApiResponse.builder()
                .message("Lấy danh sách bài kiểm tra thành công")
                .data(responseList)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse getAssignmentById(Long id) {
        AssignmentEntity assignmentEntity = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));

        return ApiResponse.builder()
                .message("OK")
                .data(toAssignmentResponse(assignmentEntity))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse submitAssignment(Long assignmentId, AssignmentSubmissionRequest req) {
        AssignmentEntity assignmentEntity = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));

        if (assignmentEntity.getDueDate() != null && LocalDateTime.now().isAfter(assignmentEntity.getDueDate())) {
            return ApiResponse.builder()
                    .message("Đã quá hạn nộp bài")
                    .build();
        }

        UserEntity userEntity = userRepository.findById(HARDCODE_STUDENT_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));

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
                .content(req.getContent())
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
    public ApiResponse getSubmissions(Long assignmentId) {
        AssignmentEntity assignmentEntity = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra"));

        List<AssignmentSubmissionEntity> list = assignmentSubmissionRepository.findByAssignment_Id(assignmentId);
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
        AssignmentSubmissionEntity submissionEntity = assignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài nộp không tồn tại"));
        if (!submissionEntity.getAssignment().getId().equals(assignmentId)) {
            return ApiResponse.builder().message("Bài nộp không thuộc bài kiểm tra này").build();
        }

        submissionEntity.setScore(score);
        assignmentSubmissionRepository.save(submissionEntity);
        return ApiResponse.builder()
                .message("Chấm điểm hoàn tất")
                .data(toAssignmentSubmissionResponse(submissionEntity))
                .build();
    }

    private AssignmentResponse toAssignmentResponse(AssignmentEntity entity) {
        return AssignmentResponse.builder()
                .id(entity.getId())
                .classId(entity.getClassroom() != null ? entity.getClassroom().getId() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .dueDate(entity.getDueDate())
                .createdByUserId(entity.getUserCreated() != null ? entity.getUserCreated().getId() : null)
                .build();
    }

    private AssignmentSubmissionResponse toAssignmentSubmissionResponse(AssignmentSubmissionEntity entity) {
        return AssignmentSubmissionResponse.builder()
                .id(entity.getId())
                .assignmentId(entity.getAssignment().getId())
                .userId(entity.getUser().getId())
                .content(entity.getContent())
                .score(entity.getScore())
                .submittedAt(entity.getSubmittedAt())
                .build();
    }
}
