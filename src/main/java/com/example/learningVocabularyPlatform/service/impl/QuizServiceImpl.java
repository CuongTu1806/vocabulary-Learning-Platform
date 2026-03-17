package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.QuizRequest;
import com.example.learningVocabularyPlatform.dto.response.QuizResponse;
import com.example.learningVocabularyPlatform.entity.UserVocabularyEntity;
import com.example.learningVocabularyPlatform.entity.VocabularyEntity;
import com.example.learningVocabularyPlatform.enums.QuizType;
import com.example.learningVocabularyPlatform.repository.QuizRepository;
import com.example.learningVocabularyPlatform.repository.UserVocabularyRepository;
import com.example.learningVocabularyPlatform.repository.VocabularyRepository;
import com.example.learningVocabularyPlatform.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;

    @Override
    public List<QuizResponse> getQuiz(long lessonId, long userId, QuizRequest request) {
        List<UserVocabularyEntity> questions = userVocabularyRepository.findByLesson_Id(lessonId);
        QuizType quizType = parseQuizType(request.getQuizType());

        return questions.stream()
                .map(question -> buildQuizResponse(question, lessonId, userId, quizType))
                .collect(Collectors.toList());
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

    private QuizResponse buildQuizResponse(UserVocabularyEntity q, long lessonId, long userId, QuizType quizType) {
        String correctAnswer;
        String questionText;
        List<String> distractors = Collections.emptyList();

        switch (quizType) {
            case ENG_TO_VN:
                questionText = q.getWord();
                correctAnswer = q.getMeaning();
                distractors = pickDistractors(q, correctAnswer, true);
                break;
            case VN_TO_ENG:
                questionText = q.getMeaning();
                correctAnswer = q.getWord();
                distractors = pickDistractors(q, correctAnswer, false);
                break;
            case VN_FILL_ENG:
                questionText = q.getMeaning();
                correctAnswer = q.getWord();
                break;
            default:
                throw new IllegalArgumentException("Unsupported quiz type: " + quizType);
        }

        return QuizResponse.builder()
                .quizType(quizType.name())
                .lessonId(lessonId)
                .userId(userId)
                .question(questionText)
                .distractor(distractors)
                .correctAnswer(correctAnswer)
                .build();
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

        List<String> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);
        return shuffled.stream().limit(3).collect(Collectors.toList());
    }

    private boolean isValidCandidate(String candidate, String correctAnswer) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        return !candidate.trim().equalsIgnoreCase(correctAnswer == null ? "" : correctAnswer.trim());
    }
}
