package com.example.learningVocabularyPlatform.service.impl;

import com.example.learningVocabularyPlatform.dto.request.ChangePasswordRequest;
import com.example.learningVocabularyPlatform.dto.request.LoginRequest;
import com.example.learningVocabularyPlatform.dto.request.RefreshTokenRequest;
import com.example.learningVocabularyPlatform.dto.request.RegisterRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.AuthResponse;
import com.example.learningVocabularyPlatform.entity.RefreshTokenEntity;
import com.example.learningVocabularyPlatform.entity.UserEntity;
import com.example.learningVocabularyPlatform.repository.RefreshTokenRepository;
import com.example.learningVocabularyPlatform.repository.UserRepository;
import com.example.learningVocabularyPlatform.service.AuthService;
import com.example.learningVocabularyPlatform.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtils jwtUtils;

	@Override
	public ApiResponse register(RegisterRequest request) {
		validateRegisterRequest(request);

		String username = request.getUsername().trim();
		String email = request.getEmail().trim();

		if (userRepository.existsByUsername(username)) {
			throw new IllegalArgumentException("Username already exists");
		}

		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("Email already exists");
		}

		UserEntity user = UserEntity.builder()
				.username(username)
				.email(email)
				.password(passwordEncoder.encode(request.getPassword()))
				.role("USER")
				.build();

		userRepository.save(user);

		return ApiResponse.builder()
				.message("Register success")
				.data(null)
				.build();
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
			throw new IllegalArgumentException("Username and password are required");
		}

		UserEntity user = userRepository.findByUsername(request.getUsername().trim())
				.orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Invalid username or password");
		}

		revokeAllUserRefreshTokens(user);
		return buildAuthResponse(user);
	}

	@Override
	public AuthResponse refreshToken(RefreshTokenRequest request) {
		if (request == null || !StringUtils.hasText(request.getRefreshToken())) {
			throw new IllegalArgumentException("Refresh token is required");
		}

		String refreshToken = request.getRefreshToken().trim();
		RefreshTokenEntity tokenEntity = refreshTokenRepository.findByToken(refreshToken)
				.orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

		if (tokenEntity.isRevoked()) {
			throw new IllegalArgumentException("Refresh token has been revoked");
		}

		if (jwtUtils.isTokenExpired(refreshToken)) {
			tokenEntity.setRevoked(true);
			refreshTokenRepository.save(tokenEntity);
			throw new IllegalArgumentException("Refresh token has expired");
		}

		UserEntity user = tokenEntity.getUser();
		if (user == null) {
			throw new IllegalArgumentException("Invalid refresh token");
		}

		tokenEntity.setRevoked(true);
		refreshTokenRepository.save(tokenEntity);

		return buildAuthResponse(user);
	}

	@Override
	public ApiResponse logout(RefreshTokenRequest request) {
		if (request == null || !StringUtils.hasText(request.getRefreshToken())) {
			throw new IllegalArgumentException("Refresh token is required");
		}

		String refreshToken = request.getRefreshToken().trim();
		RefreshTokenEntity tokenEntity = refreshTokenRepository.findByToken(refreshToken)
				.orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

		tokenEntity.setRevoked(true);
		refreshTokenRepository.save(tokenEntity);

		return ApiResponse.builder()
				.message("Logout success")
				.data(null)
				.build();
	}

	@Override
	public ApiResponse changePassword(String username, ChangePasswordRequest request) {
		if (!StringUtils.hasText(username)) {
			throw new IllegalArgumentException("Unauthorized");
		}

		if (request == null
				|| !StringUtils.hasText(request.getOldPassword())
				|| !StringUtils.hasText(request.getNewPassword())
				|| !StringUtils.hasText(request.getConfirmPassword())) {
			throw new IllegalArgumentException("Old password, new password and confirm password are required");
		}

		if (!request.getNewPassword().equals(request.getConfirmPassword())) {
			throw new IllegalArgumentException("Confirm password does not match");
		}

		if (request.getNewPassword().length() < 6) {
			throw new IllegalArgumentException("New password must be at least 6 characters");
		}

		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Old password is incorrect");
		}

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
		revokeAllUserRefreshTokens(user);

		return ApiResponse.builder()
				.message("Change password success")
				.data(null)
				.build();
	}

	private AuthResponse buildAuthResponse(UserEntity user) {
		String accessToken = jwtUtils.generateAccessToken(user.getUsername());
		String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

		RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
				.token(refreshToken)
				.expiryDate(LocalDateTime.now().plusNanos(jwtUtils.getRefreshJwtExpirationMs() * 1_000_000L))
				.revoked(false)
				.user(user)
				.build();
		refreshTokenRepository.save(tokenEntity);

		return AuthResponse.builder()
				.token(accessToken)
				.refreshToken(refreshToken)
				.tokenType("Bearer")
				.userId(user.getId())
				.username(user.getUsername())
				.email(user.getEmail())
				.build();
	}

	private void revokeAllUserRefreshTokens(UserEntity user) {
		var activeTokens = refreshTokenRepository.findAllByUser_IdAndRevokedFalse(user.getId());
		activeTokens.forEach(tokenEntity -> tokenEntity.setRevoked(true));
		refreshTokenRepository.saveAll(activeTokens);
	}

	@Override
	public ApiResponse forgotPassword(String email) {
		if (!StringUtils.hasText(email)) {
			throw new IllegalArgumentException("Email is required");
		}

		UserEntity user = userRepository.findByEmail(email.trim())
				.orElseThrow(() -> new IllegalArgumentException("Email not found in system"));

		// TODO: Send reset password email with token
		// For now, return success message
		return ApiResponse.builder()
				.message("Password reset link sent to email. Please check your email.")
				.data(null)
				.build();
	}

	@Override
	public AuthResponse getUserProfile(String username) {
		if (!StringUtils.hasText(username)) {
			throw new IllegalArgumentException("Username is required");
		}

		UserEntity user = userRepository.findByUsername(username.trim())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		return AuthResponse.builder()
				.userId(user.getId())
				.username(user.getUsername())
				.email(user.getEmail())
				.tokenType("Bearer")
				.build();
	}

	private void validateRegisterRequest(RegisterRequest request) {
		if (request == null
				|| !StringUtils.hasText(request.getUsername())
				|| !StringUtils.hasText(request.getEmail())
				|| !StringUtils.hasText(request.getPassword())) {
			throw new IllegalArgumentException("Username, email and password are required");
		}

		if (request.getPassword().length() < 6) {
			throw new IllegalArgumentException("Password must be at least 6 characters");
		}
	}
}
