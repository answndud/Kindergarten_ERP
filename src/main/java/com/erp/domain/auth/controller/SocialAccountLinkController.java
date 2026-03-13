package com.erp.domain.auth.controller;

import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberAuthProvider;
import com.erp.global.security.AuthenticatedMemberResolver;
import com.erp.global.security.oauth2.OAuth2LinkSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class SocialAccountLinkController {

    private final AuthenticatedMemberResolver authenticatedMemberResolver;
    private final OAuth2LinkSessionService oauth2LinkSessionService;

    @GetMapping("/auth/social/link/{provider}")
    public String startLink(@PathVariable String provider,
                            Authentication authentication,
                            HttpServletRequest request) {
        Member member = authenticatedMemberResolver.resolve(authentication).orElse(null);
        if (member == null) {
            return "redirect:/login";
        }

        MemberAuthProvider targetProvider = resolveProvider(provider);
        if (targetProvider == null) {
            return "redirect:/settings?socialLinkStatus=error&reason=unsupported";
        }

        if (member.isLinkedTo(targetProvider)) {
            return "redirect:/settings?socialLinkStatus=info&reason=already-linked&provider=" + provider.toLowerCase();
        }

        if (member.hasLinkedSocialAccount()) {
            return "redirect:/settings?socialLinkStatus=error&reason=slot-occupied";
        }

        oauth2LinkSessionService.store(request, member.getId(), targetProvider);
        return "redirect:/oauth2/authorization/" + provider.toLowerCase();
    }

    private MemberAuthProvider resolveProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return null;
        }

        try {
            MemberAuthProvider resolved = MemberAuthProvider.valueOf(provider.trim().toUpperCase());
            return resolved == MemberAuthProvider.LOCAL ? null : resolved;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
