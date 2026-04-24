package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.request.ContestRequest;
import com.example.learningVocabularyPlatform.dto.request.ContestSingleAnswerRequest;
import com.example.learningVocabularyPlatform.dto.request.ContestSubmitRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.FileDownloadDto;
import org.springframework.web.multipart.MultipartFile;

public interface ContestService {

    ApiResponse createContest(ContestRequest req);

    ApiResponse updateContest(Long id, ContestRequest req);

    ApiResponse deleteContest(Long id);

    ApiResponse getContests();

    ApiResponse getContestById(Long id);

    ApiResponse registerContest(Long contestId);

    ApiResponse submitContest(Long contestId, ContestSubmitRequest req);

    /** Nộp một câu — trả về đúng/sai + tổng điểm (cho hiệu ứng từng bước). */
    ApiResponse submitSingleAnswer(Long contestId, Long problemId, ContestSingleAnswerRequest req);

    /** Điểm, hạng, tiến độ của user hiện tại trong contest. */
    ApiResponse getMyContestStats(Long contestId);

    ApiResponse getRanking(Long contestId);

    ApiResponse uploadProblemImage(Long contestId, Long problemId, MultipartFile file);

    FileDownloadDto downloadProblemImage(Long contestId, Long problemId);
}
