package com.erp.domain.auth.service;

import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberStatus;
import com.erp.domain.member.service.MemberService;
import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import com.erp.global.security.jwt.JwtProperties;
import com.erp.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;
import java.util.UUID;

/**
 * 인증 서비스
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh:session:";
    private static final String REFRESH_SESSION_SET_KEY_PREFIX = "refresh:sessions:";

    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthLoginBootstrapService authLoginBootstrapService;
    private final AuthRateLimitService authRateLimitService;

    /**
     * 회원가입
     */
    @Transactional
    public Long signUp(String email, String password, String name, String phone, String role) {
        if (role == null || role.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "역할은 필수입니다");
        }

        com.erp.domain.member.entity.MemberRole memberRole;
        try {
            memberRole = com.erp.domain.member.entity.MemberRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "역할 값이 올바르지 않습니다");
        }

        return memberService.signUp(email, password, name, phone, memberRole);
    }

    /**
     * 로그인
     */
    public void login(String email, String password, String clientIp, HttpServletResponse response) {
        try {
            authRateLimitService.validateLoginAllowed(clientIp, email);

            // 1. 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            authLoginBootstrapService.afterAuthenticated(email);

            // 2. 회원 정보 조회
            Member member = memberService.getMemberByEmail(email);
            issueTokens(member, response);

        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    public void loginBySocial(Member member, HttpServletResponse response) {
        authLoginBootstrapService.afterAuthenticated(member.getEmail());
        issueTokens(member, response);
    }

    /**
     * 로그아웃
     */
    public void logout(String refreshToken, HttpServletResponse response) {
        revokeSession(refreshToken);
        expireCookie(response, jwtTokenProvider.getAccessTokenCookieName());
        expireCookie(response, jwtTokenProvider.getRefreshTokenCookieName());
    }

    /**
     * Access Token 갱신
     */
    public void refreshAccessToken(String refreshToken, String clientIp, HttpServletResponse response) {
        authRateLimitService.validateRefreshAllowed(clientIp);

        TokenSessionClaims claims = extractRefreshTokenClaims(refreshToken);
        String refreshTokenKey = getRefreshTokenKey(claims.memberId(), claims.sessionId());
        Object savedRefreshToken = redisTemplate.opsForValue().get(refreshTokenKey);
        if (!(savedRefreshToken instanceof String storedToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        if (!storedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        Member member = memberService.getMemberById(claims.memberId());
        validateRefreshTargetMember(member, claims.email());

        String newAccessToken = jwtTokenProvider.createAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole().getKey(),
                claims.sessionId()
        );
        String newRefreshToken = jwtTokenProvider.createRefreshToken(
                member.getId(),
                member.getEmail(),
                member.getRole().getKey(),
                claims.sessionId()
        );

        saveRefreshSession(member.getId(), claims.sessionId(), newRefreshToken);

        addCookie(response, jwtTokenProvider.getAccessTokenCookieName(), newAccessToken,
                (int) (jwtTokenProvider.getAccessTokenValidity() / 1000));
        addCookie(response, jwtTokenProvider.getRefreshTokenCookieName(), newRefreshToken,
                (int) (jwtTokenProvider.getRefreshTokenValidity() / 1000));
    }

    public void revokeAllSessions(Long memberId, HttpServletResponse response) {
        revokeAllSessions(memberId);
        expireCookie(response, jwtTokenProvider.getAccessTokenCookieName());
        expireCookie(response, jwtTokenProvider.getRefreshTokenCookieName());
    }

    /**
     * 쿠키 추가
     */
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(jwtProperties.isCookieSecure());
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", jwtProperties.getCookieSameSite());
        response.addCookie(cookie);
    }

    /**
     * 쿠키 만료
     */
    private void expireCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(jwtProperties.isCookieSecure());
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", jwtProperties.getCookieSameSite());
        response.addCookie(cookie);
    }

    private String getRefreshTokenKey(Long memberId, String sessionId) {
        return REFRESH_TOKEN_KEY_PREFIX + memberId + ":" + sessionId;
    }

    private String getRefreshSessionSetKey(Long memberId) {
        return REFRESH_SESSION_SET_KEY_PREFIX + memberId;
    }

    private void issueTokens(Member member, HttpServletResponse response) {
        String sessionId = UUID.randomUUID().toString();
        String accessToken = jwtTokenProvider.createAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole().getKey(),
                sessionId
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(
                member.getId(),
                member.getEmail(),
                member.getRole().getKey(),
                sessionId
        );

        saveRefreshSession(member.getId(), sessionId, refreshToken);

        addCookie(response, jwtTokenProvider.getAccessTokenCookieName(), accessToken,
                (int) (jwtTokenProvider.getAccessTokenValidity() / 1000));
        addCookie(response, jwtTokenProvider.getRefreshTokenCookieName(), refreshToken,
                (int) (jwtTokenProvider.getRefreshTokenValidity() / 1000));
    }

    private void saveRefreshSession(Long memberId, String sessionId, String refreshToken) {
        long ttlMs = Math.max(jwtTokenProvider.getRemainingValidity(refreshToken), 1L);

        redisTemplate.opsForValue().set(
                getRefreshTokenKey(memberId, sessionId),
                refreshToken,
                ttlMs,
                TimeUnit.MILLISECONDS
        );
        redisTemplate.opsForSet().add(getRefreshSessionSetKey(memberId), sessionId);
        redisTemplate.expire(getRefreshSessionSetKey(memberId), ttlMs, TimeUnit.MILLISECONDS);
    }

    private void revokeSession(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            return;
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        String sessionId = jwtTokenProvider.getSessionId(refreshToken);
        if (memberId == null || sessionId == null || sessionId.isBlank()) {
            return;
        }

        redisTemplate.delete(getRefreshTokenKey(memberId, sessionId));
        String sessionSetKey = getRefreshSessionSetKey(memberId);
        redisTemplate.opsForSet().remove(sessionSetKey, sessionId);

        Long remainingSessions = redisTemplate.opsForSet().size(sessionSetKey);
        if (remainingSessions == null || remainingSessions == 0) {
            redisTemplate.delete(sessionSetKey);
        }
    }

    private void revokeAllSessions(Long memberId) {
        String sessionSetKey = getRefreshSessionSetKey(memberId);
        var sessionIds = redisTemplate.opsForSet().members(sessionSetKey);
        if (sessionIds != null) {
            for (Object sessionId : sessionIds) {
                if (sessionId != null) {
                    redisTemplate.delete(getRefreshTokenKey(memberId, sessionId.toString()));
                }
            }
        }
        redisTemplate.delete(sessionSetKey);
    }

    private TokenSessionClaims extractRefreshTokenClaims(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        String email = jwtTokenProvider.getEmail(refreshToken);
        String sessionId = jwtTokenProvider.getSessionId(refreshToken);

        if (memberId == null || email == null || email.isBlank() || sessionId == null || sessionId.isBlank()) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        return new TokenSessionClaims(memberId, email, sessionId);
    }

    private void validateRefreshTargetMember(Member member, String email) {
        if (!member.getEmail().equals(email)
                || member.getDeletedAt() != null
                || member.getStatus() == MemberStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    private record TokenSessionClaims(Long memberId, String email, String sessionId) {
    }
}
