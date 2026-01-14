package com.erp.domain.auth.controller;

import com.erp.global.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 인증 뷰 컨트롤러
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthViewController {

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping("/signup")
    public String signUpPage() {
        return "auth/signup";
    }

    /**
     * 메인 페이지 (임시)
     * 인증되지 않은 사용자도 접근 가능 (랜딩 페이지)
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * 프로필 페이지
     */
    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        return "auth/profile";
    }

    /**
     * 설정 페이지
     */
    @GetMapping("/settings")
    public String settingsPage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        return "auth/settings";
    }
}
