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
    void testAuditLogsPageWithoutAuth() throws Exception {
        mockMvc.perform(get("/audit-logs"))
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
                .andExpect(content().string(containsString("다른 로그인 수단을 먼저 확보해야 연결을 해제할 수 있습니다.")))
                .andExpect(content().string(not(containsString("현재 비밀번호"))));
    }

    @Test
    void testSettingsPageForPrincipalShowsAuditLogLink() throws Exception {
        Kindergarten kindergarten = kindergartenRepository.save(
                Kindergarten.create("원장 설정 유치원", "서울시", "010-5555-6666", LocalTime.of(9, 0), LocalTime.of(18, 0))
        );

        Member principal = Member.create(
                "principal-settings@test.com",
                "encoded-password",
                "설정원장",
                "01022223333",
                MemberRole.PRINCIPAL
        );
        principal.assignKindergarten(kindergarten);
        memberRepository.save(principal);

        mockMvc.perform(get("/settings").with(user(new CustomUserDetails(principal))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("운영 도구")))
                .andExpect(content().string(containsString("인증 감사 로그 열기")));
    }

    @Test
    void testSettingsPageWithLocalPasswordAndLinkedSocialAccountShowsUnlinkButton() throws Exception {
        Kindergarten kindergarten = kindergartenRepository.save(
                Kindergarten.create("연결해제 유치원", "서울시", "010-7777-8888", LocalTime.of(9, 0), LocalTime.of(18, 0))
        );

        Member linkedMember = Member.createSocial(
                "unlink-view@test.com",
                "연결해제화면회원",
                MemberRole.PARENT,
                MemberAuthProvider.KAKAO,
                "kakao-view-123"
        );
        linkedMember.changePassword("encoded-local-password");
        linkedMember.assignKindergarten(kindergarten);
        memberRepository.save(linkedMember);

        mockMvc.perform(get("/settings").with(user(new CustomUserDetails(linkedMember))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("연결됨: Kakao")))
                .andExpect(content().string(containsString("연결 해제")))
                .andExpect(content().string(containsString("다른 로그인 수단이 남아 있어 연결 해제를 허용합니다.")));
    }

    @Test
    void testSettingsPageShowsReconnectGuidanceForHistoricallyLinkedProvider() throws Exception {
        Kindergarten kindergarten = kindergartenRepository.save(
                Kindergarten.create("재연결 정책 유치원", "서울시", "010-1212-3434", LocalTime.of(9, 0), LocalTime.of(18, 0))
        );

        Member relinkMember = Member.createSocial(
                "relink-policy@test.com",
                "재연결정책회원",
                MemberRole.PARENT,
                MemberAuthProvider.GOOGLE,
                "google-relink-123"
        );
        relinkMember.changePassword("encoded-local-password");
        relinkMember.assignKindergarten(kindergarten);
        relinkMember.unlinkSocialAccount(MemberAuthProvider.GOOGLE);
        memberRepository.save(relinkMember);

        mockMvc.perform(get("/settings").with(user(new CustomUserDetails(relinkMember))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Google 재연결")))
                .andExpect(content().string(containsString("처음 연결했던 동일한 Google 계정만 다시 연결할 수 있습니다.")))
                .andExpect(content().string(not(containsString("Google 연결됨"))));
    }

    @Test
    void testSettingsPageWithMultipleLinkedSocialAccountsShowsBothProviders() throws Exception {
        Kindergarten kindergarten = kindergartenRepository.save(
                Kindergarten.create("다중 연결 유치원", "서울시", "010-9999-1111", LocalTime.of(9, 0), LocalTime.of(18, 0))
        );

        Member multiLinkedMember = Member.createSocial(
                "multi-linked@test.com",
                "다중연결회원",
                MemberRole.PARENT,
                MemberAuthProvider.GOOGLE,
                "google-multi-view-123"
        );
        multiLinkedMember.linkSocialAccount(MemberAuthProvider.KAKAO, "kakao-multi-view-456");
        multiLinkedMember.assignKindergarten(kindergarten);
        memberRepository.save(multiLinkedMember);

        mockMvc.perform(get("/settings").with(user(new CustomUserDetails(multiLinkedMember))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("연결됨: Google, Kakao")))
                .andExpect(content().string(containsString("Google 연결됨")))
                .andExpect(content().string(containsString("Kakao 연결됨")))
                .andExpect(content().string(containsString("다른 로그인 수단이 남아 있어 연결 해제를 허용합니다.")));
    }

    @Test
    void testAuditLogsPageForPrincipal() throws Exception {
        Kindergarten kindergarten = kindergartenRepository.save(
                Kindergarten.create("감사 로그 유치원", "서울시", "010-8989-7878", LocalTime.of(9, 0), LocalTime.of(18, 0))
        );

        Member principal = Member.create(
                "audit-principal@test.com",
                "encoded-password",
                "감사원장",
                "01011112222",
                MemberRole.PRINCIPAL
        );
        principal.assignKindergarten(kindergarten);
        memberRepository.save(principal);

        mockMvc.perform(get("/audit-logs").with(user(new CustomUserDetails(principal))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("인증 감사 로그")))
                .andExpect(content().string(containsString("API 계약 보기")))
                .andExpect(content().string(containsString("로그인, refresh, 소셜 연결/해제 이벤트")));
    }

    @Test
    void testAuditLogsPageForTeacherForbidden() throws Exception {
        Kindergarten kindergarten = kindergartenRepository.save(
                Kindergarten.create("감사 로그 권한 유치원", "서울시", "010-6767-5656", LocalTime.of(9, 0), LocalTime.of(18, 0))
        );

        Member teacher = Member.create(
                "audit-teacher@test.com",
                "encoded-password",
                "감사교사",
                "01033334444",
                MemberRole.TEACHER
        );
        teacher.assignKindergarten(kindergarten);
        memberRepository.save(teacher);

        mockMvc.perform(get("/audit-logs").with(user(new CustomUserDetails(teacher))))
                .andExpect(status().isForbidden());
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
