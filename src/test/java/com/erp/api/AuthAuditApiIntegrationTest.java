package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.authaudit.entity.AuthAuditEventType;
import com.erp.domain.authaudit.entity.AuthAuditLog;
import com.erp.domain.authaudit.entity.AuthAuditResult;
import com.erp.domain.authaudit.repository.AuthAuditLogRepository;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberAuthProvider;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthAuditApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthAuditLogRepository authAuditLogRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("원장은 자기 유치원 소속 member 기반 감사 로그만 조회할 수 있다")
    void getAuditLogs_Success_PrincipalOnlySeesOwnKindergartenLogs() throws Exception {
        authAuditLogRepository.saveAndFlush(AuthAuditLog.create(
                teacherMember.getId(),
                teacherMember.getEmail(),
                MemberAuthProvider.LOCAL,
                AuthAuditEventType.LOGIN,
                AuthAuditResult.SUCCESS,
                null,
                "198.51.100.10"
        ));

        Kindergarten otherKindergarten = kindergartenRepository.save(
                Kindergarten.create("다른 유치원", "부산시", "010-2222-3333", LocalTime.of(9, 0), LocalTime.of(18, 0))
        );
        Member otherPrincipal = testData.createTestMember("other-principal@test.com", "다른원장", MemberRole.PRINCIPAL, "test1234");
        otherPrincipal.assignKindergarten(otherKindergarten);
        memberRepository.saveAndFlush(otherPrincipal);

        authAuditLogRepository.saveAndFlush(AuthAuditLog.create(
                otherPrincipal.getId(),
                otherPrincipal.getEmail(),
                MemberAuthProvider.LOCAL,
                AuthAuditEventType.LOGIN,
                AuthAuditResult.FAILURE,
                "A001",
                "203.0.113.22"
        ));

        authAuditLogRepository.saveAndFlush(AuthAuditLog.create(
                null,
                "unknown@test.com",
                MemberAuthProvider.LOCAL,
                AuthAuditEventType.LOGIN,
                AuthAuditResult.FAILURE,
                "A001",
                "203.0.113.77"
        ));

        mockMvc.perform(get("/api/v1/auth/audit-logs")
                        .with(authenticated(principalMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].email").value("teacher@test.com"))
                .andExpect(jsonPath("$.data.content[0].eventType").value("LOGIN"))
                .andExpect(jsonPath("$.data.content[0].result").value("SUCCESS"));
    }

    @Test
    @DisplayName("원장은 eventType, result, provider, email, date 필터로 감사 로그를 조회할 수 있다")
    void getAuditLogs_Success_WithFilters() throws Exception {
        authAuditLogRepository.saveAndFlush(AuthAuditLog.create(
                teacherMember.getId(),
                teacherMember.getEmail(),
                MemberAuthProvider.GOOGLE,
                AuthAuditEventType.SOCIAL_UNLINK,
                AuthAuditResult.FAILURE,
                "A010",
                "198.51.100.12"
        ));
        authAuditLogRepository.saveAndFlush(AuthAuditLog.create(
                parentMember.getId(),
                parentMember.getEmail(),
                MemberAuthProvider.LOCAL,
                AuthAuditEventType.LOGIN,
                AuthAuditResult.SUCCESS,
                null,
                "198.51.100.14"
        ));

        mockMvc.perform(get("/api/v1/auth/audit-logs")
                        .with(authenticated(principalMember))
                        .param("eventType", "SOCIAL_UNLINK")
                        .param("result", "FAILURE")
                        .param("provider", "GOOGLE")
                        .param("email", "teacher")
                        .param("from", LocalDate.now().minusDays(1).toString())
                        .param("to", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].reason").value("A010"))
                .andExpect(jsonPath("$.data.content[0].provider").value("GOOGLE"));
    }

    @Test
    @DisplayName("교사는 인증 감사 로그 조회가 차단된다")
    void getAuditLogs_Fail_TeacherForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/auth/audit-logs")
                        .with(authenticated(teacherMember)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("A004"));
    }
}
