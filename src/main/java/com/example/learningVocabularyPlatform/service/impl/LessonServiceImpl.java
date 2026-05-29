package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.LessonRequest;
import com.example.learningVocabularyPlatform.dto.request.VocabularyAddRequest;
import com.example.learningVocabularyPlatform.dto.response.LessonResponse;
import com.example.learningVocabularyPlatform.dto.response.UserVocabularyResponse;
import com.example.learningVocabularyPlatform.entity.*;
import com.example.learningVocabularyPlatform.exception.ResourceNotFoundException;
import com.example.learningVocabularyPlatform.mapper.LessonMapper;
import com.example.learningVocabularyPlatform.mapper.VocabularyMapper;
import com.example.learningVocabularyPlatform.repository.*;
import com.example.learningVocabularyPlatform.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    private static final String LESSON_NOT_FOUND = "Lesson not found";
    private static final String LESSON_NOT_OWNED = "Ban khong co quyen sua xoa bai hoc nay";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String LESSON_PUBLIC = "PUBLIC";

    private final LessonRepository lessonRepository;
    private final LessonAccessRepository lessonAccessRepository;
    private final LessonMapper lessonMapper;
    private final UserRepository userRepository;
    private final VocabularyMapper vocabularyMapper;
    private final UserVocabularyRepository userVocabularyRepository;
    private final ReviewScheduleRepository reviewScheduleRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final LessonDownloadRepository lessonDownloadRepository;

    //Get all lesson belong to user
    @Override
    @Transactional(readOnly = true)
    public List<LessonResponse> getAll(Long userId) {
        // tạo map lưu lesson id và lesson entity
        Map<Long, LessonEntity> lessonMap = new LinkedHashMap<>();

        // lấy lesson do user current
        for (LessonEntity lesson : lessonRepository.findByUser_Id(userId)) {
            lessonMap.putIfAbsent(lesson.getId(), lesson);
        }

        // lấy lesson mà user tải về
        for (LessonEntity lesson : lessonAccessRepository.findAccessibleLessonsByUserId(userId)) {
            lessonMap.putIfAbsent(lesson.getId(), lesson);
        }

        // đuừa vào response
        List<LessonResponse> res = new ArrayList<>();
        for (LessonEntity lesson : lessonMap.values()) {
            res.add(lessonMapper.convertLessonToResponse(lesson));
        }
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonResponse> searchPublicLessons(String query) {
        List<LessonEntity> lessons = lessonRepository.searchPublicLessons(query != null ? query.trim() : null);
        List<LessonResponse> res = new ArrayList<>();
        for (LessonEntity lesson : lessons) {
            res.add(lessonMapper.convertLessonToResponse(lesson));
        }
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public LessonResponse getLesson(Long lessonId, Long userId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(LESSON_NOT_FOUND));
        boolean isOwner = lesson.getUser() != null && lesson.getUser().getId().equals(userId);
        boolean isPublic = LESSON_PUBLIC.equalsIgnoreCase(lesson.getVisibility());
        boolean hasAccess = lessonAccessRepository.existsByUser_IdAndLesson_Id(userId, lessonId);
        if (!isOwner && !isPublic && !hasAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ban khong co quyen xem bai hoc nay");
        }
        LessonResponse response = lessonMapper.convertLessonToResponse(lesson);
        response.setCurrentUserCanQuiz(isOwner || hasAccess);
        return response;
    }

    @Override
    @Transactional
    public LessonResponse importLesson(Long sourceLessonId, Long userId) {
        // lấy tuwf db lesson cần tải
        LessonEntity sourceLesson = lessonRepository.findById(sourceLessonId)
                .orElseThrow(() -> new ResourceNotFoundException(LESSON_NOT_FOUND));
        if (!LESSON_PUBLIC.equalsIgnoreCase(sourceLesson.getVisibility())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chi co the tai ve bai hoc cong khai");
        }

        UserEntity owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        // Nếu user đã có quyền truy cập (đã tải trước đó) thì chặn tải lại
        if (lessonAccessRepository.existsByUser_IdAndLesson_Id(userId, sourceLessonId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bạn đã tải bài này rồi");
        }

        // Ghi log mọi lượt tải để phục vụ leaderboard theo tuần/tháng/all-time
        lessonDownloadRepository.save(LessonDownloadEntity.builder()
            .user(owner)
            .lesson(sourceLesson)
            .build());
        sourceLesson.setDownloadCount((sourceLesson.getDownloadCount() != null ? sourceLesson.getDownloadCount() : 0) + 1);
        lessonRepository.save(sourceLesson);

        // Cấp quyền truy cập lần đầu (đã đảm bảo chưa có trước đó)
        lessonAccessRepository.save(LessonAccessEntity.builder()
                .user(owner)
                .lesson(sourceLesson)
                .build());

        ensureReviewSchedulesForLesson(owner, sourceLesson);

        return lessonMapper.convertLessonToResponse(sourceLesson);
    }

    // create new lesson
    @Override
    public LessonResponse createLesson(Long userId, LessonRequest lessonRequest) {
        LessonEntity ls = lessonMapper.convertRequestToLessonEntity(lessonRequest);
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        ls.setUser(user);
        if (ls.getVisibility() == null || ls.getVisibility().isBlank()) {
            ls.setVisibility("PRIVATE");
        }
        ls = lessonRepository.save(ls);
        return lessonMapper.convertLessonToResponse(ls);
    }

    // add a vocabulary into lesson
    @Override
    public UserVocabularyResponse addVocab(Long lessonId, VocabularyAddRequest request, Long userId) {
        UserVocabularyEntity uvc = vocabularyMapper.convertRequestToUserVocab(request);
        LessonEntity lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException(LESSON_NOT_FOUND));
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        if (lesson.getUser() == null || !lesson.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, LESSON_NOT_OWNED);
        }
        uvc.setUser(user);
        uvc.setLesson(lesson);
        uvc.setStatus("learning");
        uvc = userVocabularyRepository.save(uvc);

        ensureReviewScheduleForUserVocabulary(user, uvc);
        return vocabularyMapper.convertUserVocabularyToResponse(uvc);
    }

    // update title and description of lesson
    @Override
    public LessonResponse updateLesson(Long userId, Long lessonId, LessonRequest request) {
        LessonEntity lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException(LESSON_NOT_FOUND));
        if (lesson.getUser() == null || !lesson.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, LESSON_NOT_OWNED);
        }
        if(request.getTitle() != null) lesson.setTitle(request.getTitle());
        if(request.getDescription() != null) lesson.setDescription(request.getDescription());
        if (request.getVisibility() != null && !request.getVisibility().isBlank()) {
            lesson.setVisibility(request.getVisibility().trim().toUpperCase());
        }
        lessonRepository.save(lesson);
        return lessonMapper.convertLessonToResponse(lesson);
    }

    // delete a lesson, it will delete all vocabulary of this lesson
    @Override
    public void deleteLesson(Long userId, Long lessonId) {
        LessonEntity lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException(LESSON_NOT_FOUND));
        if (lesson.getUser() == null || !lesson.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, LESSON_NOT_OWNED);
        }
        reviewHistoryRepository.deleteByReviewSchedule_UserVocabulary_Lesson_Id(lessonId);
        reviewScheduleRepository.deleteByUserVocabulary_Lesson_Id(lessonId);
        lessonAccessRepository.deleteByLesson_Id(lessonId);
        userVocabularyRepository.deleteByLesson_Id(lessonId);
        lessonRepository.deleteById(lessonId);
    }

    private void ensureReviewSchedulesForLesson(UserEntity user, LessonEntity lesson) {
        List<UserVocabularyEntity> vocabularies = lesson.getUserVocabularies();
        if (vocabularies == null || vocabularies.isEmpty()) {
            return;
        }
        for (UserVocabularyEntity userVocabulary : vocabularies) {
            ensureReviewScheduleForUserVocabulary(user, userVocabulary);
        }
    }

    private void ensureReviewScheduleForUserVocabulary(UserEntity user, UserVocabularyEntity userVocabulary) {
        if (reviewScheduleRepository.existsByUser_IdAndUserVocabulary_Id(user.getId(), userVocabulary.getId())) {
            return;
        }

        ReviewScheduleEntity reviewScheduleEntity = ReviewScheduleEntity.builder()
            .user(user)
            .userVocabulary(userVocabulary)
            .intervalDays(-1)
            .nextReviewDate(LocalDateTime.now())
            .lastReviewDate(null)
            .easeFactor(1.5)
            .delayFactor(0.05)
            .learningStep(1)
            .state("learning")
            .build();
        reviewScheduleRepository.save(reviewScheduleEntity);
    }

    // get all vocab in lesson
    @Override
    @Transactional(readOnly = true)
    public List<UserVocabularyResponse> getVocabInLesson(Long lessonId, Long userId) {
        LessonEntity lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException(LESSON_NOT_FOUND));
        boolean isOwner = lesson.getUser() != null && lesson.getUser().getId().equals(userId);
        boolean isPublic = LESSON_PUBLIC.equalsIgnoreCase(lesson.getVisibility());
        boolean hasAccess = lessonAccessRepository.existsByUser_IdAndLesson_Id(userId, lessonId);
        if (!isOwner && !isPublic && !hasAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ban khong co quyen xem bai hoc nay");
        }
        List<UserVocabularyEntity> list = lesson.getUserVocabularies();
        List<UserVocabularyResponse> responses = new ArrayList<>();

        for(UserVocabularyEntity uvc : list){
            if(uvc.getVocabulary() != null){
                responses.add(vocabularyMapper.convertVocabularyToResponse(uvc.getVocabulary()));
                responses.getLast().setStatus(uvc.getStatus());
            }
            else responses.add(vocabularyMapper.convertUserVocabularyToResponse(uvc));
            responses.getLast().setLessonId(lessonId);
        }
        return responses;
    }
}
