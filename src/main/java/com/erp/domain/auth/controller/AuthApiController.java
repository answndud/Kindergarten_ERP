package com.erp.domain.auth.controller;

import com.erp.domain.auth.dto.request.LoginRequest;
import com.erp.domain.auth.dto.request.SignUpRequest;
import com.erp.domain.member.dto.response.MemberResponse;
import com.erp.domain.auth.service.AuthService;
import com.erp.domain.member.service.MemberService;
import com.erp.global.common.ApiResponse;
import com.erp.global.security.jwt.JwtTokenProvider;
import com.erp.global.security.user.CustomUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        // 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(
                            com.erp.global.exception.ErrorCode.INVALID_INPUT_VALUE,
                            "비밀번호가 일치하지 않습니다"
                    ));
        }

        // 회원가입
        Long memberId = authService.signUp(
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getPhone(),
                request.getRole()
        );

        // 회원 정보 조회
        com.erp.domain.member.entity.Member member = memberService.getMemberById(memberId);

        return ResponseEntity
                .ok(ApiResponse.success(MemberResponse.from(member), "회원가입이 완료되었습니다"));
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest request,
                                                    HttpServletResponse response) {
        authService.login(request.getEmail(), request.getPassword(), response);

        return ResponseEntity
                .ok(ApiResponse.success(null, "로그인되었습니다"));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails.getUsername();
        authService.logout(email, response);

        return ResponseEntity
                .ok(ApiResponse.success(null, "로그아웃되었습니다"));
    }

    /**
     * Access Token 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(HttpServletRequest request,
                                                      HttpServletResponse response) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = getRefreshToken(request);

        if (refreshToken == null) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(com.erp.global.exception.ErrorCode.TOKEN_INVALID));
        }

        authService.refreshAccessToken(refreshToken, response);

        return ResponseEntity
                .ok(ApiResponse.success(null, "토큰이 갱신되었습니다"));
    }

    /**
     * 현재 로그인한 회원 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getCurrentMember(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        com.erp.domain.member.entity.Member member = memberService.getMemberById(userDetails.getMemberId());

        return ResponseEntity
                .ok(ApiResponse.success(MemberResponse.from(member)));
    }

    /**
     * 쿠키에서 Refresh Token 추출
     */
    private String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (jwtTokenProvider.getRefreshTokenCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
