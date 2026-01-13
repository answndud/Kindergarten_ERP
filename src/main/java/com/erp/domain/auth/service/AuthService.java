package com.erp.domain.auth.service;

import com.erp.domain.member.entity.Member;
import com.erp.domain.member.service.MemberService;
import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import com.erp.global.security.jwt.JwtTokenProvider;
import com.erp.global.security.user.CustomUserDetailsService;
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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 인증 서비스
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 회원가입
     */
    @Transactional
    public Long signUp(String email, String password, String name, String phone, String role) {
        // 역할 변환
        com.erp.domain.member.entity.MemberRole memberRole =
                com.erp.domain.member.entity.MemberRole.valueOf(role);

        return memberService.signUp(email, password, name, phone, memberRole);
    }

    /**
     * 로그인
     */
    public void login(String email, String password, HttpServletResponse response) {
        try {
            // 1. 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // 2. 회원 정보 조회
            Member member = memberService.getMemberByEmail(email);

            // 3. 토큰 생성
            String accessToken = jwtTokenProvider.createAccessToken(email, member.getRole().getKey());
            String refreshToken = jwtTokenProvider.createRefreshToken(email, member.getRole().getKey());

            // 4. Refresh Token을 Redis에 저장
            String tokenId = UUID.randomUUID().toString();
            String refreshTokenKey = "refresh:" + email + ":" + tokenId;
            redisTemplate.opsForValue().set(
                    refreshTokenKey,
                    refreshToken,
                    7,
                    TimeUnit.DAYS
            );

            // 5. 쿠키에 토큰 저장
            addCookie(response, jwtTokenProvider.getAccessTokenCookieName(), accessToken,
                    (int) (jwtTokenProvider.getAccessTokenValidity() / 1000));
            addCookie(response, jwtTokenProvider.getRefreshTokenCookieName(), refreshToken,
                    (int) (jwtTokenProvider.getRefreshTokenValidity() / 1000));

        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    /**
     * 로그아웃
     */
    public void logout(String email, HttpServletResponse response) {
        // 1. Redis에서 Refresh Token 삭제 (모든 토큰)
        redisTemplate.delete(redisTemplate.keys("refresh:" + email + ":*"));

        // 2. 쿠키 삭제
        expireCookie(response, jwtTokenProvider.getAccessTokenCookieName());
        expireCookie(response, jwtTokenProvider.getRefreshTokenCookieName());
    }

    /**
     * Access Token 갱신
     */
    public void refreshAccessToken(String refreshToken, HttpServletResponse response) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        // 2. 이메일 추출
        String email = jwtTokenProvider.getEmail(refreshToken);

        // 3. Redis에서 Refresh Token 존재 확인
        Boolean exists = redisTemplate.hasKey("refresh:" + email + ":*");
        if (exists == null || !exists) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        // 4. 회원 정보 조회
        Member member = memberService.getMemberByEmail(email);

        // 5. 새 Access Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(email, member.getRole().getKey());

        // 6. 쿠키에 새 Access Token 저장
        addCookie(response, jwtTokenProvider.getAccessTokenCookieName(), newAccessToken,
                (int) (jwtTokenProvider.getAccessTokenValidity() / 1000));
    }

    /**
     * 쿠키 추가
     */
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬 개발용 (운영은 true)
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    /**
     * 쿠키 만료
     */
    private void expireCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
