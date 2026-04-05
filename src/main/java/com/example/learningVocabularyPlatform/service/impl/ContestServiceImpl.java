package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.ContestAnswerItem;
import com.example.learningVocabularyPlatform.dto.request.ContestProblemRequest;
import com.example.learningVocabularyPlatform.dto.request.ContestRequest;
import com.example.learningVocabularyPlatform.dto.request.ContestSubmitRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.ContestProblemResponse;
import com.example.learningVocabularyPlatform.dto.response.ContestRankingResponse;
import com.example.learningVocabularyPlatform.dto.response.ContestResponse;
import com.example.learningVocabularyPlatform.dto.response.ContestSubmissionResponse;
import com.example.learningVocabularyPlatform.entity.*;
import com.example.learningVocabularyPlatform.exception.ResourceNotFoundException;
import com.example.learningVocabularyPlatform.repository.*;
import com.example.learningVocabularyPlatform.service.ContestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContestServiceImpl implements ContestService {

    private final ContestRepository contestRepository;
    private final ContestProblemRepository contestProblemRepository;
    private final ContestParticipantRepository contestParticipantRepository;
    private final ContestSubmissionRepository contestSubmissionRepository;
    private final UserRepository userRepository;

    private static final Long HARDCODE_CREATOR_ID = 1L;
    private static final Long HARDCODE_STUDENT_ID = 4L;

    @Override
    @Transactional
    public ApiResponse createContest(ContestRequest req) {
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            return ApiResponse.builder()
                    .message("Thời gian kết thúc phải sau thời gian bắt đầu")
                    .build();
        }

        UserEntity creator = userRepository.findById(HARDCODE_CREATOR_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        ContestEntity contest = ContestEntity.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .visibility(req.getVisibility() != null ? req.getVisibility() : "PUBLIC")
                .userCreated(creator)
                .build();
        contestRepository.save(contest);

        // Tạo kèm danh sách câu (nếu có) — cùng transaction: lỗi thì rollback cả contest
        if (req.getProblems() != null && !req.getProblems().isEmpty()) {
            for (ContestProblemRequest p : req.getProblems()) {
                ContestProblemEntity problem = ContestProblemEntity.builder()
                        .title(p.getTitle())
                        .description(p.getDescription())
                        .wrongAnswer(p.getWrongAnswer())
                        .answer(p.getAnswer())
                        .difficulty(p.getDifficulty())
                        .maxScore(p.getMaxScore())
                        .orderIndex(p.getOrderIndex())
                        .contest(contest)
                        .build();
                contestProblemRepository.save(problem);
            }
        }

        return ApiResponse.builder()
                .message("Tạo cuộc thi thành công")
                .data(toContestResponse(contest, true))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse updateContest(Long id, ContestRequest req) {
        ContestEntity contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc thi"));

        if (req.getTitle() != null) {
            contest.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            contest.setDescription(req.getDescription());
        }
        if (req.getVisibility() != null) {
            contest.setVisibility(req.getVisibility());
        }
        if (req.getStartTime() != null) {
            contest.setStartTime(req.getStartTime());
        }
        if (req.getEndTime() != null) {
            contest.setEndTime(req.getEndTime());
        }

        LocalDateTime start = contest.getStartTime();
        LocalDateTime end = contest.getEndTime();
        if (start != null && end != null && !end.isAfter(start)) {
            return ApiResponse.builder()
                    .message("Thời gian kết thúc phải sau thời gian bắt đầu")
                    .build();
        }

        // Không tự động sửa danh sách câu ở đây — tránh xóa nhầm; thêm API riêng nếu cần
        return ApiResponse.builder()
                .message("Cập nhật cuộc thi thành công")
                .data(toContestResponse(contest, true))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse deleteContest(Long id) {
        ContestEntity contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc thi"));

        // submission trỏ contest + problem → xóa submission trước, sau đó problem, participant, cuối cùng contest.
        contestSubmissionRepository.deleteAll(contestSubmissionRepository.findByContest_Id(id));
        contestProblemRepository.deleteAll(contestProblemRepository.findByContest_IdOrderByOrderIndexAsc(id));
        contestParticipantRepository.deleteAll(contestParticipantRepository.findByContest_Id(id));
        contestRepository.delete(contest);

        return ApiResponse.builder().message("Đã xóa cuộc thi").build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getContests() {
        List<ContestResponse> list = contestRepository.findAll().stream()
                .map(c -> toContestResponse(c, false))
                .toList();
        return ApiResponse.builder()
                .message("OK")
                .data(list)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getContestById(Long id) {
        ContestEntity contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc thi"));
        return ApiResponse.builder()
                .message("OK")
                .data(toContestResponse(contest, true))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse registerContest(Long contestId) {
        ContestEntity contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc thi"));

        UserEntity user = userRepository.findById(HARDCODE_STUDENT_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (contestParticipantRepository.existsByContest_IdAndUser_Id(contestId, user.getId())) {
            return ApiResponse.builder().message("Bạn đã đăng ký cuộc thi này rồi").build();
        }

        ContestParticipantEntity row = ContestParticipantEntity.builder()
                .contest(contest)
                .user(user)
                .registeredAt(LocalDateTime.now())
                .build();
        contestParticipantRepository.save(row);

        return ApiResponse.builder().message("Đăng ký thành công").build();
    }

    @Override
    @Transactional
    public ApiResponse submitContest(Long contestId, ContestSubmitRequest req) {
        ContestEntity contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc thi"));

        UserEntity user = userRepository.findById(HARDCODE_STUDENT_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(contest.getStartTime()) || now.isAfter(contest.getEndTime())) {
            return ApiResponse.builder().message("Không trong thời gian diễn ra cuộc thi").build();
        }

        if (!contestParticipantRepository.existsByContest_IdAndUser_Id(contestId, user.getId())) {
            return ApiResponse.builder().message("Bạn cần đăng ký cuộc thi trước khi nộp bài").build();
        }

        List<ContestSubmissionResponse> results = new ArrayList<>();

        for (ContestAnswerItem item : req.getAnswers()) {
            ContestProblemEntity problem = contestProblemRepository.findById(item.getProblemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi: " + item.getProblemId()));

            if (!problem.getContest().getId().equals(contestId)) {
                return ApiResponse.builder()
                        .message("Câu hỏi không thuộc cuộc thi này: " + item.getProblemId())
                        .build();
            }

            if (contestSubmissionRepository
                    .findByContest_IdAndProblem_IdAndUser_Id(contestId, problem.getId(), user.getId())
                    .isPresent()) {
                return ApiResponse.builder()
                        .message("Bạn đã nộp câu " + problem.getId() + " rồi")
                        .build();
            }

            int score = grade(problem.getAnswer(), item.getUserAnswer(), problem.getMaxScore());

            ContestSubmissionEntity submission = ContestSubmissionEntity.builder()
                    .contest(contest)
                    .user(user)
                    .problem(problem)
                    .userAnswer(item.getUserAnswer() != null ? item.getUserAnswer() : "")
                    .score(score)
                    .status("SUBMITTED")
                    .submittedAt(now)
                    .build();
            contestSubmissionRepository.save(submission);
            results.add(toSubmissionResponse(submission));
        }

        return ApiResponse.builder()
                .message("Nộp bài thành công")
                .data(results)
                .build();
    }

    // So sánh đáp án không phân biệt hoa thường
    private int grade(String correct, String userAnswer, int maxScore) {
        if (correct == null) {
            return 0;
        }
        String ua = userAnswer == null ? "" : userAnswer.trim();
        if (ua.equalsIgnoreCase(correct.trim())) {
            return maxScore;
        }
        return 0;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getRanking(Long contestId) {
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("Không tìm thấy cuộc thi");
        }

        List<ContestSubmissionEntity> subs = contestSubmissionRepository.findByContest_Id(contestId);

        // Gom điểm theo user_id (một user có nhiều dòng submission — mỗi dòng một câu)
        Map<Long, Integer> totalByUser = subs.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getUser().getId(),
                        Collectors.summingInt(ContestSubmissionEntity::getScore)));

        if (totalByUser.isEmpty()) {
            return ApiResponse.builder().message("Chưa có bài nộp").data(List.of()).build();
        }

        // Lấy username theo batch
        List<Long> userIds = new ArrayList<>(totalByUser.keySet());
        Map<Long, UserEntity> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        // Sắp xếp: điểm giảm dần, cùng điểm thì userId tăng dần (ổn định)
        List<Map.Entry<Long, Integer>> sorted = totalByUser.entrySet().stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getValue(), a.getValue());
                    if (cmp != 0) {
                        return cmp;
                    }
                    return Long.compare(a.getKey(), b.getKey());
                })
                .toList();

        List<ContestRankingResponse> ranking = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<Long, Integer> e = sorted.get(i);
            // Xếp hạng kiểu thi đấu: cùng điểm thì cùng hạng (1,1,3,...)
            if (i > 0 && !sorted.get(i).getValue().equals(sorted.get(i - 1).getValue())) {
                rank = i + 1;
            }
            Long uid = e.getKey();
            UserEntity u = users.get(uid);
            ranking.add(ContestRankingResponse.builder()
                    .rank(rank)
                    .userId(uid)
                    .username(u != null ? u.getUsername() : null)
                    .totalScore(e.getValue())
                    .build());
        }

        return ApiResponse.builder().message("OK").data(ranking).build();
    }

    /** includeProblems=true: load câu hỏi (ẩn đáp án đúng); false: list contest nhẹ */
    private ContestResponse toContestResponse(ContestEntity c, boolean includeProblems) {
        List<ContestProblemResponse> problems = null;
        if (includeProblems) {
            problems = contestProblemRepository.findByContest_IdOrderByOrderIndexAsc(c.getId()).stream()
                    .map(this::toProblemResponsePublic)
                    .toList();
        }
        return ContestResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .startTime(c.getStartTime())
                .endTime(c.getEndTime())
                .visibility(c.getVisibility())
                .createdByUserId(c.getUserCreated() != null ? c.getUserCreated().getId() : null)
                .problems(problems)
                .build();
    }

    /** Không map field answer — tránh lộ đáp án qua GET */
    private ContestProblemResponse toProblemResponsePublic(ContestProblemEntity p) {
        return ContestProblemResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .wrongAnswer(p.getWrongAnswer())
                .difficulty(p.getDifficulty())
                .maxScore(p.getMaxScore())
                .orderIndex(p.getOrderIndex())
                .build();
    }

    private ContestSubmissionResponse toSubmissionResponse(ContestSubmissionEntity s) {
        return ContestSubmissionResponse.builder()
                .id(s.getId())
                .contestId(s.getContest().getId())
                .problemId(s.getProblem().getId())
                .userId(s.getUser().getId())
                .userAnswer(s.getUserAnswer())
                .score(s.getScore())
                .status(s.getStatus())
                .submittedAt(s.getSubmittedAt())
                .build();
    }
}
