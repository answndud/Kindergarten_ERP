package com.erp.domain.auth.controller;

import com.erp.global.security.AuthenticatedMemberResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

/**
 * 인증 뷰 컨트롤러
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthenticatedMemberResolver authenticatedMemberResolver;

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            Model model) {
        resolveLoginError(error).ifPresent(loginError -> {
            model.addAttribute("loginErrorTitle", loginError.title());
            model.addAttribute("loginErrorMessage", loginError.message());
            model.addAttribute("loginErrorHint", loginError.hint());
        });
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
    public String profilePage(Authentication authentication) {
        if (authenticatedMemberResolver.resolve(authentication).isEmpty()) {
            return "redirect:/login";
        }
        return "auth/profile";
    }

    /**
     * 설정 페이지
     */
    @GetMapping("/settings")
    public String settingsPage(Authentication authentication) {
        if (authenticatedMemberResolver.resolve(authentication).isEmpty()) {
            return "redirect:/login";
        }
        return "auth/settings";
    }

    private Optional<LoginErrorContent> resolveLoginError(String error) {
        if (error == null || error.isBlank()) {
            return Optional.empty();
        }

        return switch (error) {
            case "social_account_conflict" -> Optional.of(new LoginErrorContent(
                    "이미 가입된 계정이 있습니다",
                    "같은 이메일의 기존 계정이 있어 소셜 계정을 자동으로 연결하지 않았습니다.",
                    "기존 로그인 방식으로 로그인해 주세요. 자동 연결은 보안상 허용하지 않습니다."
            ));
            case "social_login_failed" -> Optional.of(new LoginErrorContent(
                    "소셜 로그인에 실패했습니다",
                    "소셜 로그인 처리 중 문제가 발생했습니다.",
                    "잠시 후 다시 시도해 주세요."
            ));
            default -> Optional.empty();
        };
    }

    private record LoginErrorContent(String title, String message, String hint) {
    }
}
