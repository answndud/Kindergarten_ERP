package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import com.erp.global.security.user.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("교사 지원 API 테스트")
class KindergartenApplicationApiIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("교사 지원 승인 - 성공")
    void approveKindergartenApplication_Success() throws Exception {
        Member applicantTeacher = testData.createTestMember("apply-teacher@test.com", "신규교사", MemberRole.TEACHER, "test1234");

        String applyBody = """
                {
                    "kindergartenId": 1,
                    "message": "지원합니다"
                }
                """;

        MvcResult applyResult = mockMvc.perform(post("/api/v1/kindergarten-applications")
                        .with(user(new CustomUserDetails(applicantTeacher)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        long applicationId = objectMapper.readTree(applyResult.getResponse().getContentAsString())
                .path("data")
                .asLong();

        mockMvc.perform(put("/api/v1/kindergarten-applications/{id}/approve", applicationId)
                        .with(user(new CustomUserDetails(principalMember)))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Member updatedTeacher = memberRepository.findById(applicantTeacher.getId()).orElseThrow();
        assertTrue(updatedTeacher.getKindergarten() != null);
    }

    @Test
    @DisplayName("교사 지원 신청 - 실패 (학부모 권한 없음)")
    void applyKindergartenApplication_Fail_Parent() throws Exception {
        String applyBody = """
                {
                    "kindergartenId": 1,
                    "message": "학부모 권한 테스트"
                }
                """;

        mockMvc.perform(post("/api/v1/kindergarten-applications")
                        .with(user(new CustomUserDetails(parentMember)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
