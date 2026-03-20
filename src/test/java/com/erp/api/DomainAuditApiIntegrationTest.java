package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.domainaudit.entity.DomainAuditAction;
import com.erp.domain.domainaudit.entity.DomainAuditTargetType;
import com.erp.domain.domainaudit.service.DomainAuditLogService;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("업무 감사 로그 API 테스트")
@Tag("integration")
class DomainAuditApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DomainAuditLogService domainAuditLogService;

    @Test
    @DisplayName("원장은 필터로 업무 감사 로그를 조회할 수 있다")
    void getDomainAuditLogs_Success_WithActionFilter() throws Exception {
        Member applicant = testData.createTestMember("domain-audit-parent@test.com", "감사학부모", MemberRole.PARENT, "test1234");

        String applyBody = """
                {
                    "kindergartenId": 1,
                    "kidName": "감사원아",
                    "birthDate": "2020-02-02",
                    "gender": "FEMALE",
                    "preferredClassroomId": 1
                }
                """;

        String response = mockMvc.perform(post("/api/v1/kid-applications")
                        .with(authenticated(applicant))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long applicationId = objectMapper.readTree(response).path("data").asLong();

        mockMvc.perform(put("/api/v1/kid-applications/{id}/waitlist", applicationId)
                        .with(authenticated(principalMember))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "classroomId": 1,
                                    "decisionNote": "정원 만석"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/domain-audit-logs")
                        .with(authenticated(principalMember))
                        .param("action", "KID_APPLICATION_WAITLISTED")
                        .param("from", LocalDate.now().minusDays(1).toString())
                        .param("to", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].action").value("KID_APPLICATION_WAITLISTED"))
                .andExpect(jsonPath("$.data.content[0].targetType").value("KID_APPLICATION"));
    }

    @Test
    @DisplayName("원장은 업무 감사 로그 CSV를 export할 수 있다")
    void exportDomainAuditLogs_Success() throws Exception {
        domainAuditLogService.record(
                principalMember,
                kindergarten.getId(),
                DomainAuditAction.ANNOUNCEMENT_UPDATED,
                DomainAuditTargetType.ANNOUNCEMENT,
                announcement.getId(),
                "공지사항 수정",
                java.util.Map.of("important", true)
        );

        mockMvc.perform(get("/api/v1/domain-audit-logs/export")
                        .with(authenticated(principalMember))
                        .param("action", "ANNOUNCEMENT_UPDATED"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment;")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(containsString("createdAt,action,targetType,targetId,actorName,actorRole,summary,metadataJson")))
                .andExpect(content().string(containsString("\"ANNOUNCEMENT_UPDATED\"")));
    }

    @Test
    @DisplayName("교사는 업무 감사 로그 조회가 차단된다")
    void getDomainAuditLogs_Fail_TeacherForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/domain-audit-logs")
                        .with(authenticated(teacherMember)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("A004"));
    }
}
