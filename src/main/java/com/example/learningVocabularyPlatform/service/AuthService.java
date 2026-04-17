package com.example.learningVocabularyPlatform.service;

import com.example.learningVocabularyPlatform.dto.request.ChangePasswordRequest;
import com.example.learningVocabularyPlatform.dto.request.LoginRequest;
import com.example.learningVocabularyPlatform.dto.request.RefreshTokenRequest;
import com.example.learningVocabularyPlatform.dto.request.RegisterRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.AuthResponse;

public interface AuthService {
	ApiResponse register(RegisterRequest request);

	AuthResponse login(LoginRequest request);

	AuthResponse refreshToken(RefreshTokenRequest request);

	ApiResponse logout(RefreshTokenRequest request);

	ApiResponse changePassword(String username, ChangePasswordRequest request);

	ApiResponse forgotPassword(String email);

	AuthResponse getUserProfile(String username);
}
