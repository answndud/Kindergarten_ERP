package com.erp.integration;

import com.erp.common.TestcontainersSupport;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kindergarten.repository.KindergartenRepository;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberAuthProvider;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.repository.MemberRepository;
import com.erp.global.security.user.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 뷰 엔드포인트 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ViewEndpointTest extends TestcontainersSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private KindergartenRepository kindergartenRepository;

    @Test
    void testHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginPageWithSocialAccountConflictError() throws Exception {
        mockMvc.perform(get("/login").param("error", "social_account_conflict"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("이미 가입된 계정이 있습니다")))
                .andExpect(content().string(containsString("소셜 계정을 자동으로 연결하지 않았습니다.")))
                .andExpect(content().string(containsString("기존 로그인 방식으로 로그인해 주세요.")));
    }

    @Test
    void testSignupPage() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk());
    }

    // 인증이 필요한 페이지들 (로그인 없으면 리다이렉트되어야 함)
    @Test
    void testNotepadPageWithoutAuth() throws Exception {
        mockMvc.perform(get("/notepad"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testAttendancePageWithoutAuth() throws Exception {
        mockMvc.perform(get("/attendance"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testAnnouncementsPageWithoutAuth() throws Exception {
        mockMvc.perform(get("/announcements"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testProfilePageWithoutAuth() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testSettingsPageWithoutAuth() throws Exception {
        mockMvc.perform(get("/settings"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testSocialLinkStartRedirectsToOauthAuthorization() throws Exception {
        Kindergarten kindergarten = kindergartenRepository.save(
                Kindergarten.create("연결 유치원", "서울시", "010-2222-3333", LocalTime.of(9, 0), LocalTime.of(18, 0))
        );

        Member localMember = Member.create(
                "link-local@test.com",
                "encoded-password",
                "로컬회원",
                "01033334444",
                MemberRole.PARENT
        );
        localMember.assignKindergarten(kindergarten);
        memberRepository.save(localMember);

        mockMvc.perform(get("/auth/social/link/google").with(user(new CustomUserDetails(localMember))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/google"))
                .andExpect(request().sessionAttribute("oauth2_link_member_id", localMember.getId()))
                .andExpect(request().sessionAttribute("oauth2_link_provider", "GOOGLE"));
    }

    @Test
    void testSettingsPageForSocialOnlyAccountShowsBootstrapPasswordForm() throws Exception {
        Kindergarten kindergarten = kindergartenRepository.save(
                Kindergarten.create("소셜 설정 유치원", "서울시", "010-4444-5555", LocalTime.of(9, 0), LocalTime.of(18, 0))
        );

        Member socialMember = Member.createSocial(
                "settings-social@test.com",
                "소셜설정회원",
                MemberRole.PARENT,
                MemberAuthProvider.GOOGLE,
                "settings-google-123"
        );
        socialMember.assignKindergarten(kindergarten);
        memberRepository.save(socialMember);

        mockMvc.perform(get("/settings").with(user(new CustomUserDetails(socialMember))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("연결됨: Google")))
                .andExpect(content().string(containsString("소셜 로그인 전용")))
                .andExpect(content().string(containsString("로컬 비밀번호 설정")))
                .andExpect(content().string(not(containsString("현재 비밀번호"))));
    }

    @Test
    void testProfilePageWithOAuth2Principal() throws Exception {
        Kindergarten kindergarten = kindergartenRepository.save(
                Kindergarten.create("소셜 유치원", "서울시", "010-1111-2222", LocalTime.of(9, 0), LocalTime.of(18, 0))
        );

        Member socialMember = Member.createSocial(
                "social-parent@test.com",
                "소셜학부모",
                MemberRole.PARENT,
                MemberAuthProvider.GOOGLE,
                "google-sub-123"
        );
        socialMember.assignKindergarten(kindergarten);
        memberRepository.save(socialMember);

        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_PARENT")),
                Map.of(
                        "sub", "google-sub-123",
                        "email", "social-parent@test.com",
                        "name", "소셜학부모"
                ),
                "sub"
        );

        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                principal,
                principal.getAuthorities(),
                "google"
        );

        mockMvc.perform(get("/profile").with(authentication(authentication)))
                .andExpect(status().isOk());
    }
}
