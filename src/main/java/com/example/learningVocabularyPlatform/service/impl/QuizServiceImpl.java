package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.AnswerSubmitRequest;
import com.example.learningVocabularyPlatform.dto.request.QuizHistoryRequest;
import com.example.learningVocabularyPlatform.dto.request.QuizRequest;
import com.example.learningVocabularyPlatform.dto.request.QuizSubmitRequest;
import com.example.learningVocabularyPlatform.dto.response.*;
import com.example.learningVocabularyPlatform.entity.*;
import com.example.learningVocabularyPlatform.enums.QuizType;
import com.example.learningVocabularyPlatform.mapper.QuizMapper;
import com.example.learningVocabularyPlatform.repository.*;
import com.example.learningVocabularyPlatform.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizMapper quizMapper;

    @Override
    public QuizResponse getQuiz(long lessonId, long userId, QuizRequest request) {
        // lấy thông tin user
        UserEntity userEntity = userRepository.findById(userId).orElse(null);

        // lấy thoog tin lesson
        LessonEntity lessonEntity = lessonRepository.findById(lessonId).orElse(null);

        // lấy danh sách vocabulary thuộc về lesson đang ôn tập
        List<UserVocabularyEntity> questions = userVocabularyRepository.findByLesson_Id(lessonId);

        // kiểm tra loại quiz
        QuizType quizType = parseQuizType(request.getQuizType());

        QuizEntity qe = QuizEntity.builder()
                .typeQuiz(quizType)
                .user(userEntity)
                .lesson(lessonEntity)
                .finishedAt(null)
                .duration(null)
                .build();
        // lưu thông tin quiz
        quizRepository.save(qe);

        // Danh sách câu hỏi và các đáp án chọn
        List<QuizQuestionResponse> qqrs = new ArrayList<>();

        String correctAnswer;
        String content;

        // kiểm tra quiz type để lấy question
        switch (quizType) {
            // if eng to vn -> useMeaning = true -> get meaning for distractor
            case ENG_TO_VN:
                for(UserVocabularyEntity q : questions) {
                    correctAnswer = q.getMeaning();
                    content = q.getWord();
                    List<String> options = pickDistractors(q, correctAnswer, true);
                    
                    QuizResultEntity qre = QuizResultEntity.builder()
                            .quiz(qe)
                            .userVocabulary(q)
                            .content(content)
                            .trueAnswer(correctAnswer)
                            .options(options)
                            .build();
                    quizResultRepository.save(qre);
                    
                    qqrs.add(QuizQuestionResponse.builder()
                            .quizResultId(qre.getId())
                            .answers(options)
                            .content(content)
                            .build());
                }
                break;
            // if vn to eng -> useMeaning = false -> get word for distractor
            case VN_TO_ENG:
                for(UserVocabularyEntity q : questions) {
                    correctAnswer = q.getWord();
                    content = q.getMeaning();
                    List<String> options = pickDistractors(q, correctAnswer, false);
                    
                    QuizResultEntity qre = QuizResultEntity.builder()
                            .quiz(qe)
                            .userVocabulary(q)
                            .content(content)
                            .trueAnswer(correctAnswer)
                            .options(options)
                            .build();
                    quizResultRepository.save(qre);

                    qqrs.add(QuizQuestionResponse.builder()
                            .quizResultId(qre.getId())
                            .answers(options)
                            .content(content)
                            .build());
                }
                break;
            case VN_FILL_ENG:
                for(UserVocabularyEntity q : questions) {
                    List<String> options = new ArrayList<>();
                    options.add(q.getWord());
                    
                    QuizResultEntity qre = QuizResultEntity.builder()
                            .quiz(qe)
                            .userVocabulary(q)
                            .content(q.getMeaning())
                            .trueAnswer(q.getWord())
                            .options(options)
                            .build();
                    quizResultRepository.save(qre);

                    qqrs.add(QuizQuestionResponse.builder()
                            .quizResultId(qre.getId())
                            .content(q.getMeaning())
                            .build());
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported quiz type: " + quizType);
        }


        return QuizResponse.builder()
                .questions(qqrs)
                .quizType(String.valueOf(quizType))
                .userId(userId)
                .lessonId(lessonId)
                .build();
    }

    @Override
    public QuizSubmitResponse submitQuiz(long userId, QuizSubmitRequest request) {
        Long quizId = request.getQuizId();

        if (quizId == null) {
            throw new IllegalArgumentException("quizId is required");
        }

        QuizEntity quizEntity = quizRepository.findById(quizId).orElse(null);
        if (quizEntity == null) {
            throw new IllegalArgumentException("Quiz not found for id: " + quizId);
        }

        List<QuizResultEntity> quizResultEntities = quizResultRepository.findByQuiz_Id(quizId);
        List<AnswerSubmitRequest> answerSubmitRequests = request.getAnswers();
        if (answerSubmitRequests == null) {
            answerSubmitRequests = Collections.emptyList();
        }

        // chuyển list quiz result thành map với key là id và value là entity để lookup so đáp án nhanh không phải 2 vòng for
        Map<Long, QuizResultEntity> quizResultMap = quizResultEntities.stream()
                .collect(Collectors.toMap(QuizResultEntity::getId, q -> q));

        List<QuizResultDetailResponse> resultDetails = new ArrayList<>();

        int totalQuesttions = answerSubmitRequests.size();
        int totalCorrectAnswers = 0;
        Double correctPercentage = 0.0;


        for(AnswerSubmitRequest ans : answerSubmitRequests) {
            QuizResultEntity result = quizResultMap.get(ans.getQuestionId());
            boolean isCorrect = false;

            if (result != null && ans.getUserAnswer() != null && result.getTrueAnswer() != null) {
                isCorrect = ans.getUserAnswer().trim().equalsIgnoreCase(result.getTrueAnswer().trim());
            }

            // nếu đáp án đúng thì +1
            if(isCorrect){
                totalCorrectAnswers += 1;
            }

            // Thêm chi tiết vào list để show chi tiết bài làm
            resultDetails.add(QuizResultDetailResponse.builder()
                    .quizResultId(ans.getQuestionId())
                    .userAnswer(ans.getUserAnswer())
                    .content(result.getContent())
                    .correct(isCorrect)
                    .correctAnswer(result == null ? null : result.getTrueAnswer())
                    .build()
            );

            // lưu thông tin bài làm vào QuizResult
            if (result != null) {
                result.setUserAnswer(ans.getUserAnswer());
                result.setCorrect(isCorrect);
                quizResultRepository.save(result);
            }
        }

        if (totalQuesttions > 0) {
            correctPercentage = Math.round(((double) totalCorrectAnswers / totalQuesttions) * 10000.0) / 100.0; // two decimals percent
        } else {
            correctPercentage = 0.0;
        }

        // lưu thông tin của quiz (score lưu như phần trăm làm tròn)
        quizEntity.setScore((int) Math.round(correctPercentage));

        
        LocalDateTime finishedAt = LocalDateTime.now();
        long durationInSeconds = ChronoUnit.SECONDS.between(quizEntity.getCreatedAt(), finishedAt);
        quizEntity.setFinishedAt(finishedAt);  //Lưu thời điểm kết thúc
        quizEntity.setDuration(durationInSeconds);  //Lưu thời gian làm bài (giây)

        quizRepository.save(quizEntity);  // Lưu duration và finishedAt vào DB

        return QuizSubmitResponse.builder()
                .quizId(quizId)
                .totalQuestions(totalQuesttions)
                .correctAnswers(totalCorrectAnswers)
                .correctPercentage(correctPercentage)
                .quizResultDetailResponse(resultDetails)
                .build();
    }

    @Override
    public Page<QuizHistoryResponse> getQuizHistory(Long userId, QuizHistoryRequest req, Pageable pageable) {
        final QuizHistoryRequest request = (req == null) ? new QuizHistoryRequest() : req;

        Specification<QuizEntity> spec = (root, query, cb) -> cb.conjunction();

        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId));

        // filter lesson name
        if (request.getName() != null && !request.getName().isBlank()) {
            String lessonName = request.getName().trim().toLowerCase(Locale.ROOT);
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("lesson").get("title")),
                    "%" + lessonName + "%"));
        }
        // filter mode
        if (request.getMode() != null && !request.getMode().isBlank()) {
            QuizType mode = parseQuizType(request.getMode());
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("typeQuiz"), mode));
        }

        // filter start time
        if (request.getStartTime() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), request.getStartTime()));
        }

        // filter end time
        if (request.getEndTime() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), request.getEndTime()));
        }

        // filter min score
        if (request.getMinScore() != null) {
            int minScore = request.getMinScore().intValue();
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("score"), minScore));
        }

        // filter max score
        if (request.getMaxScore() != null) {
            int maxScore = request.getMaxScore().intValue();
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("score"), maxScore));
        }
        return quizRepository.findAll(spec, pageable)
            .map(quizMapper::toQuizHistoryResponse);

    }


    private QuizType parseQuizType(String quizTypeRaw) {
        if (quizTypeRaw == null || quizTypeRaw.isBlank()) {
            throw new IllegalArgumentException("quizType is required");
        }

        try {
            return QuizType.valueOf(quizTypeRaw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported quizType: " + quizTypeRaw);
        }
    }

    private List<String> pickDistractors(UserVocabularyEntity question, String correctAnswer, boolean useMeaning) {
        Set<String> pool = new LinkedHashSet<>();

        List<VocabularyEntity> samePos = vocabularyRepository.findByPosIgnoreCase(question.getPos());
        for (VocabularyEntity v : samePos) {
            String candidate = useMeaning ? v.getMeaning() : v.getWord();
            if (isValidCandidate(candidate, correctAnswer)) {
                pool.add(candidate.trim());
            }
        }

        // nếu đáp án nhiễu cùng pos ít hơn 3 thì lấy cái khác pos
        if (pool.size() < 3) {
            for (VocabularyEntity v : vocabularyRepository.findAll()) {
                String candidate = useMeaning ? v.getMeaning() : v.getWord();
                if (isValidCandidate(candidate, correctAnswer)) {
                    pool.add(candidate.trim());
                }
                if (pool.size() >= 3) {
                    break;
                }
            }
        }

        // trộn đáp án nhiễu
        List<String> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);
        List<String> distractors = new ArrayList<>(shuffled.subList(0, Math.min(3, shuffled.size())));
        distractors.add(correctAnswer);
        Collections.shuffle(distractors);

        return distractors;
    }

    private boolean isValidCandidate(String candidate, String correctAnswer) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        return !candidate.trim().equalsIgnoreCase(correctAnswer == null ? "" : correctAnswer.trim());
    }
}
