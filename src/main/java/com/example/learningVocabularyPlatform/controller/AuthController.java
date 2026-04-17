package com.example.learningVocabularyPlatform.controller;

import com.example.learningVocabularyPlatform.dto.request.ChangePasswordRequest;
import com.example.learningVocabularyPlatform.dto.request.LoginRequest;
import com.example.learningVocabularyPlatform.dto.request.RefreshTokenRequest;
import com.example.learningVocabularyPlatform.dto.request.RegisterRequest;
import com.example.learningVocabularyPlatform.dto.response.ApiResponse;
import com.example.learningVocabularyPlatform.dto.response.AuthResponse;
import com.example.learningVocabularyPlatform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.builder().message(ex.getMessage()).data(null).build());
		}
	}

	@PostMapping("/login")
	ResponseEntity<?> login(@RequestBody LoginRequest request) {
		try {
			AuthResponse response = authService.login(request);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.builder().message(ex.getMessage()).data(null).build());
		}
	}

	@PostMapping("/refresh")
	ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
		try {
			AuthResponse response = authService.refreshToken(request);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.builder().message(ex.getMessage()).data(null).build());
		}
	}

	@PostMapping("/logout")
	ResponseEntity<ApiResponse> logout(@RequestBody RefreshTokenRequest request) {
		try {
			return ResponseEntity.ok(authService.logout(request));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.builder().message(ex.getMessage()).data(null).build());
		}
	}

	@PostMapping("/change-password")
	ResponseEntity<ApiResponse> changePassword(Authentication authentication,
											   @RequestBody ChangePasswordRequest request) {
		if (authentication == null || authentication.getName() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.builder().message("Unauthorized").data(null).build());
		}

		try {
			return ResponseEntity.ok(authService.changePassword(authentication.getName(), request));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.builder().message(ex.getMessage()).data(null).build());
		}
	}

	@PostMapping("/forgot-password")
	ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email) {
		try {
			return ResponseEntity.ok(authService.forgotPassword(email));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.builder().message(ex.getMessage()).data(null).build());
		}
	}

	@GetMapping("/profile")
	ResponseEntity<?> getProfile(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.builder().message("Unauthorized").data(null).build());
		}

		try {
			AuthResponse response = authService.getUserProfile(authentication.getName());
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(ApiResponse.builder().message(ex.getMessage()).data(null).build());
		}
	}
}

