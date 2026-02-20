package com.erp.global.security.oauth2;

import com.erp.domain.auth.service.AuthService;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberAuthProvider;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.entity.MemberStatus;
import com.erp.domain.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final AuthService authService;

    public OAuth2AuthenticationSuccessHandler(MemberRepository memberRepository,
                                              @Lazy AuthService authService) {
        this.memberRepository = memberRepository;
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
                response.sendRedirect("/login?error=social_login_failed");
                return;
            }

            OAuth2User oAuth2User = (OAuth2User) oauthToken.getPrincipal();
            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.from(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oAuth2User.getAttributes()
            );

            Member member = memberRepository
                    .findByAuthProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
                    .orElseGet(() -> registerSocialMember(userInfo));

            authService.loginBySocial(member, response);
            response.sendRedirect(resolveRedirect(member));
        } catch (Exception ex) {
            response.sendRedirect("/login?error=social_login_failed");
        }
    }

    private Member registerSocialMember(OAuth2UserInfo userInfo) {
        String email = resolveEmail(userInfo);

        if (memberRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다: " + email);
        }

        Member member = Member.createSocial(
                email,
                userInfo.getName(),
                MemberRole.PARENT,
                userInfo.getProvider(),
                userInfo.getProviderId()
        );
        member.markPending();
        return memberRepository.save(member);
    }

    private String resolveEmail(OAuth2UserInfo userInfo) {
        if (userInfo.getEmail() != null && !userInfo.getEmail().isBlank()) {
            return userInfo.getEmail().trim().toLowerCase();
        }

        MemberAuthProvider provider = userInfo.getProvider();
        return provider.name().toLowerCase() + "_" + userInfo.getProviderId() + "@social.local";
    }

    private String resolveRedirect(Member member) {
        if (member.getRole() == MemberRole.PRINCIPAL && member.getKindergarten() == null) {
            return "/kindergarten/create";
        }
        if (member.getKindergarten() == null || member.getStatus() == MemberStatus.PENDING) {
            return "/applications/pending";
        }
        return "/";
    }
}
