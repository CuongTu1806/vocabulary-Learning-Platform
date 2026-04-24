package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.ContestAnswerItem;
import com.example.learningVocabularyPlatform.dto.request.ContestProblemRequest;
import com.example.learningVocabularyPlatform.dto.request.ContestRequest;
import com.example.learningVocabularyPlatform.dto.request.ContestSingleAnswerRequest;
import com.example.learningVocabularyPlatform.dto.request.ContestSubmitRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.ContestAnswerResultResponse;
import com.example.learningVocabularyPlatform.dto.response.ContestMyStatsResponse;
import com.example.learningVocabularyPlatform.dto.response.ContestProblemResponse;
import com.example.learningVocabularyPlatform.dto.response.ContestRankingResponse;
import com.example.learningVocabularyPlatform.dto.response.ContestResponse;
import com.example.learningVocabularyPlatform.dto.response.ContestSubmissionResponse;
import com.example.learningVocabularyPlatform.dto.response.FileDownloadDto;
import com.example.learningVocabularyPlatform.entity.*;
import com.example.learningVocabularyPlatform.exception.ResourceNotFoundException;
import com.example.learningVocabularyPlatform.repository.*;
import com.example.learningVocabularyPlatform.service.AuthenticatedUserService;
import com.example.learningVocabularyPlatform.service.ContestService;
import com.example.learningVocabularyPlatform.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final AuthenticatedUserService authenticatedUserService;
    private final FileStorageService fileStorageService;

    private static boolean isContestCreator(ContestEntity contest, UserEntity user) {
        return contest.getUserCreated() != null && contest.getUserCreated().getId().equals(user.getId());
    }

    @Override
    @Transactional
    public ApiResponse createContest(ContestRequest req) {
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            return ApiResponse.builder()
                    .message("Thời gian kết thúc phải sau thời gian bắt đầu")
                    .build();
        }

        UserEntity creator = authenticatedUserService.requireCurrentUser();

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
                        .imageUrl(p.getImageUrl())
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
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ContestEntity contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc thi"));
        if (!isContestCreator(contest, current)) {
            return ApiResponse.builder()
                    .message("Chỉ người tạo cuộc thi mới được cập nhật")
                    .build();
        }

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
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ContestEntity contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc thi"));
        if (!isContestCreator(contest, current)) {
            return ApiResponse.builder()
                    .message("Chỉ người tạo cuộc thi mới được xóa")
                    .build();
        }

        // submission trỏ contest + problem → xóa submission trước, sau đó problem, participant, cuối cùng contest.
        contestSubmissionRepository.deleteAll(contestSubmissionRepository.findByContest_Id(id));
        List<ContestProblemEntity> probRows = contestProblemRepository.findByContest_IdOrderByOrderIndexAsc(id);
        for (ContestProblemEntity p : probRows) {
            if (p.getStoredImagePath() != null && !p.getStoredImagePath().isBlank()) {
                fileStorageService.deleteIfExists(p.getStoredImagePath());
            }
        }
        contestProblemRepository.deleteAll(probRows);
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

        UserEntity user = authenticatedUserService.requireCurrentUser();

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

        UserEntity user = authenticatedUserService.requireCurrentUser();

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

    @Override
    @Transactional
    public ApiResponse submitSingleAnswer(Long contestId, Long problemId, ContestSingleAnswerRequest req) {
        ContestEntity contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc thi"));

        UserEntity user = authenticatedUserService.requireCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(contest.getStartTime()) || now.isAfter(contest.getEndTime())) {
            return ApiResponse.builder().message("Không trong thời gian diễn ra cuộc thi").build();
        }
        if (!contestParticipantRepository.existsByContest_IdAndUser_Id(contestId, user.getId())) {
            return ApiResponse.builder().message("Bạn cần đăng ký cuộc thi trước khi nộp bài").build();
        }

        ContestProblemEntity problem = contestProblemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi"));

        if (!problem.getContest().getId().equals(contestId)) {
            return ApiResponse.builder().message("Câu hỏi không thuộc cuộc thi này").build();
        }

        if (contestSubmissionRepository
                .findByContest_IdAndProblem_IdAndUser_Id(contestId, problemId, user.getId())
                .isPresent()) {
            return ApiResponse.builder().message("Bạn đã trả lời câu này rồi").build();
        }

        int score = grade(problem.getAnswer(), req.getUserAnswer(), problem.getMaxScore());
        boolean correct = score > 0;

        ContestSubmissionEntity submission = ContestSubmissionEntity.builder()
                .contest(contest)
                .user(user)
                .problem(problem)
                .userAnswer(req.getUserAnswer() != null ? req.getUserAnswer().trim() : "")
                .score(score)
                .status("SUBMITTED")
                .submittedAt(now)
                .build();
        contestSubmissionRepository.save(submission);

        int totalScore = sumScoreForUserInContest(contestId, user.getId());

        ContestAnswerResultResponse result = ContestAnswerResultResponse.builder()
                .problemId(problemId)
                .correct(correct)
                .scoreAwarded(score)
                .maxScoreForProblem(problem.getMaxScore() != null ? problem.getMaxScore() : 0)
                .totalScore(totalScore)
                .build();

        return ApiResponse.builder()
                .message(correct ? "Chính xác!" : "Chưa đúng")
                .data(result)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse getMyContestStats(Long contestId) {
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("Không tìm thấy cuộc thi");
        }
        UserEntity user = authenticatedUserService.requireCurrentUser();
        int totalProblems =
                contestProblemRepository.findByContest_IdOrderByOrderIndexAsc(contestId).size();
        List<ContestSubmissionEntity> mine =
                contestSubmissionRepository.findByContest_IdAndUser_Id(contestId, user.getId());
        int problemsAnswered = mine.size();
        int totalScore = mine.stream().mapToInt(ContestSubmissionEntity::getScore).sum();
        Integer rank = computeRankForUser(contestId, user.getId());
        List<Long> solvedIds = mine.stream()
                .map(s -> s.getProblem().getId())
                .sorted()
                .toList();

        ContestMyStatsResponse stats = ContestMyStatsResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .totalScore(totalScore)
                .rank(rank)
                .problemsAnswered(problemsAnswered)
                .totalProblems(totalProblems)
                .solvedProblemIds(solvedIds)
                .build();

        return ApiResponse.builder().message("OK").data(stats).build();
    }

    private int sumScoreForUserInContest(Long contestId, Long userId) {
        return contestSubmissionRepository.findByContest_IdAndUser_Id(contestId, userId).stream()
                .mapToInt(ContestSubmissionEntity::getScore)
                .sum();
    }

    /**
     * Hạng (1-based), cùng điểm cùng hạng; null nếu chưa có submission nào.
     */
    private Integer computeRankForUser(Long contestId, Long userId) {
        Map<Long, Integer> totalByUser = contestSubmissionRepository.findByContest_Id(contestId).stream()
                .collect(Collectors.groupingBy(
                        s -> s.getUser().getId(),
                        Collectors.summingInt(ContestSubmissionEntity::getScore)));
        if (!totalByUser.containsKey(userId)) {
            return null;
        }
        List<Map.Entry<Long, Integer>> sorted = totalByUser.entrySet().stream()
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.getValue(), a.getValue());
                    if (cmp != 0) {
                        return cmp;
                    }
                    return Long.compare(a.getKey(), b.getKey());
                })
                .toList();
        int rank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0 && !sorted.get(i).getValue().equals(sorted.get(i - 1).getValue())) {
                rank = i + 1;
            }
            if (sorted.get(i).getKey().equals(userId)) {
                return rank;
            }
        }
        return null;
    }

    // So sánh đáp án không phân biệt hoa thường
    private int grade(String correct, String userAnswer, Integer maxScore) {
        int cap = maxScore != null ? maxScore : 0;
        if (correct == null) {
            return 0;
        }
        String ua = userAnswer == null ? "" : userAnswer.trim();
        if (ua.equalsIgnoreCase(correct.trim())) {
            return cap;
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

    @Override
    @Transactional
    public ApiResponse uploadProblemImage(Long contestId, Long problemId, MultipartFile file) {
        UserEntity current = authenticatedUserService.requireCurrentUser();
        ContestProblemEntity problem = contestProblemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi"));
        if (!problem.getContest().getId().equals(contestId)) {
            return ApiResponse.builder().message("Câu hỏi không thuộc cuộc thi này").build();
        }
        ContestEntity contest = problem.getContest();
        if (!isContestCreator(contest, current)) {
            return ApiResponse.builder().message("Chỉ người tạo cuộc thi mới upload ảnh được").build();
        }
        if (file == null || file.isEmpty()) {
            return ApiResponse.builder().message("Chọn file ảnh").build();
        }
        try {
            if (problem.getStoredImagePath() != null && !problem.getStoredImagePath().isBlank()) {
                fileStorageService.deleteIfExists(problem.getStoredImagePath());
            }
            String rel = fileStorageService.store(file, "contest-problem/" + problemId);
            problem.setStoredImagePath(rel);
            problem.setImageUrl(null);
            contestProblemRepository.save(problem);
            return ApiResponse.builder().message("Upload ảnh thành công").build();
        } catch (IllegalArgumentException e) {
            return ApiResponse.builder().message(e.getMessage()).build();
        } catch (IOException e) {
            return ApiResponse.builder().message("Lỗi lưu file ảnh").build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownloadDto downloadProblemImage(Long contestId, Long problemId) {
        authenticatedUserService.requireCurrentUser();
        ContestProblemEntity problem = contestProblemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi"));
        if (!problem.getContest().getId().equals(contestId)) {
            throw new ResourceNotFoundException("Câu hỏi không thuộc cuộc thi này");
        }
        String path = problem.getStoredImagePath();
        if (path == null || path.isBlank()) {
            throw new ResourceNotFoundException("Câu hỏi không có ảnh upload");
        }
        var resource = fileStorageService.loadAsResource(path);
        String fn = path;
        int slash = Math.max(fn.lastIndexOf('/'), fn.lastIndexOf('\\'));
        if (slash >= 0 && slash < fn.length() - 1) {
            fn = fn.substring(slash + 1);
        }
        String lower = fn.toLowerCase(Locale.ROOT);
        String ct = "application/octet-stream";
        if (lower.endsWith(".png")) {
            ct = "image/png";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            ct = "image/jpeg";
        } else if (lower.endsWith(".gif")) {
            ct = "image/gif";
        } else if (lower.endsWith(".webp")) {
            ct = "image/webp";
        }
        return new FileDownloadDto(resource, fn, ct);
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

    /** Không trả answer / wrongAnswer — tránh lộ đề khi làm bài. */
    private ContestProblemResponse toProblemResponsePublic(ContestProblemEntity p) {
        boolean hasUpload = p.getStoredImagePath() != null && !p.getStoredImagePath().isBlank();
        String ext = p.getImageUrl();
        boolean showExternal =
                !hasUpload
                        && ext != null
                        && !ext.isBlank()
                        && (ext.startsWith("http://")
                                || ext.startsWith("https://")
                                || ext.startsWith("//"));
        return ContestProblemResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .imageUrl(showExternal ? ext : null)
                .hasUploadedImage(hasUpload)
                .difficulty(p.getDifficulty())
                .maxScore(p.getMaxScore())
                .orderIndex(p.getOrderIndex())
                .build();
    }

    private ContestSubmissionResponse toSubmissionResponse(ContestSubmissionEntity s) {
        boolean correct = s.getScore() > 0;
        return ContestSubmissionResponse.builder()
                .id(s.getId())
                .contestId(s.getContest().getId())
                .problemId(s.getProblem().getId())
                .userId(s.getUser().getId())
                .userAnswer(s.getUserAnswer())
                .score(s.getScore())
                .correct(correct)
                .status(s.getStatus())
                .submittedAt(s.getSubmittedAt())
                .build();
    }
}
