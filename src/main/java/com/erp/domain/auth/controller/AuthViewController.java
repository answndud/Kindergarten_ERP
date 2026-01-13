package com.erp.domain.auth.controller;

import com.erp.domain.member.dto.response.MemberResponse;
import com.erp.domain.member.service.MemberService;
import com.erp.global.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 인증 뷰 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final MemberService memberService;

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

    /**
     * 현재 로그인한 회원 정보를 모든 뷰에 전달
     */
    @ModelAttribute("currentMember")
    public MemberResponse currentMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }

        try {
            return MemberResponse.from(memberService.getMemberById(userDetails.getMemberId()));
        } catch (Exception e) {
            return null;
        }
    }
}
