package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.request.ContestRequest;
import com.example.learningVocabularyPlatform.dto.request.ContestSubmitRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;

public interface ContestService {

    ApiResponse createContest(ContestRequest req);

    ApiResponse updateContest(Long id, ContestRequest req);

    ApiResponse deleteContest(Long id);

    ApiResponse getContests();

    ApiResponse getContestById(Long id);

    ApiResponse registerContest(Long contestId);

    ApiResponse submitContest(Long contestId, ContestSubmitRequest req);

    ApiResponse getRanking(Long contestId);
}
