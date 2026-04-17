package com.example.learningVocabularyPlatform.config;

import com.example.learningVocabularyPlatform.entity.UserEntity;
import com.example.learningVocabularyPlatform.repository.UserRepository;
import com.example.learningVocabularyPlatform.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtUtils.extractUsername(token);
            if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<UserEntity> userOptional = userRepository.findByUsername(username);
                if (userOptional.isPresent() && jwtUtils.validateToken(token, username)) {
                    UserEntity user = userOptional.get();
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    user.getUsername(),
                                    null,
                                    getAuthorities(user.getRole())
                            );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private List<? extends GrantedAuthority> getAuthorities(String role) {
        if (!StringUtils.hasText(role)) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        String normalizedRole = role.toUpperCase(Locale.ROOT);
        if (!normalizedRole.startsWith("ROLE_")) {
            normalizedRole = "ROLE_" + normalizedRole;
        }

        return Collections.singletonList(new SimpleGrantedAuthority(normalizedRole));
    }
}
