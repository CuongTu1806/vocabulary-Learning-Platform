package com.example.learningVocabularyPlatform.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.jwt.expiration-ms:86400000}")
	private long jwtExpirationMs;

	@Value("${app.jwt.refresh-expiration-ms:604800000}")
	private long refreshJwtExpirationMs;

	public long getRefreshJwtExpirationMs() {
		return refreshJwtExpirationMs;
	}

	public String generateToken(String username) {
		return generateAccessToken(username);
	}

	public String generateAccessToken(String username) {
		Date now = new Date();
		Date expireDate = new Date(now.getTime() + jwtExpirationMs);

		return Jwts.builder()
				.claim("tokenType", "ACCESS")
				.subject(username)
				.issuedAt(now)
				.expiration(expireDate)
				.signWith(getSigningKey())
				.compact();
	}

	public String generateRefreshToken(String username) {
		Date now = new Date();
		Date expireDate = new Date(now.getTime() + refreshJwtExpirationMs);

		return Jwts.builder()
				.claim("tokenType", "REFRESH")
				.subject(username)
				.issuedAt(now)
				.expiration(expireDate)
				.signWith(getSigningKey())
				.compact();
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public boolean validateToken(String token, String username) {
		String tokenUsername = extractUsername(token);
		return tokenUsername.equals(username) && !isTokenExpired(token);
	}

	public boolean isTokenExpired(String token) {
		Date expiration = extractClaim(token, Claims::getExpiration);
		return expiration.before(new Date());
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
