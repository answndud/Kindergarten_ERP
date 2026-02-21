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

@DisplayName("입학 신청 API 테스트")
class KidApplicationApiIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("입학 신청 승인 - 성공 (관계값 반영)")
    void approveKidApplication_Success_WithRelationship() throws Exception {
        Member applicant = testData.createTestMember("apply-parent@test.com", "신청학부모", MemberRole.PARENT, "test1234");

        String applyBody = """
                {
                    "kindergartenId": 1,
                    "kidName": "신청원아",
                    "birthDate": "2020-02-02",
                    "gender": "FEMALE",
                    "preferredClassroomId": 1,
                    "notes": "테스트 신청"
                }
                """;

        MvcResult applyResult = mockMvc.perform(post("/api/v1/kid-applications")
                        .with(user(new CustomUserDetails(applicant)))
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

        String approveBody = """
                {
                    "classroomId": 1,
                    "relationship": "MOTHER"
                }
                """;

        mockMvc.perform(put("/api/v1/kid-applications/{id}/approve", applicationId)
                        .with(user(new CustomUserDetails(principalMember)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approveBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertTrue(parentKidRepository.findAll().stream()
                .anyMatch(parentKid -> parentKid.getParent().getId().equals(applicant.getId())
                        && "MOTHER".equals(parentKid.getRelationship().name())));
    }

    @Test
    @DisplayName("입학 신청 승인 - 실패 (학부모 권한 없음)")
    void approveKidApplication_Fail_Parent() throws Exception {
        Member applicant = testData.createTestMember("apply-parent2@test.com", "신청학부모2", MemberRole.PARENT, "test1234");

        String applyBody = """
                {
                    "kindergartenId": 1,
                    "kidName": "권한테스트원아",
                    "birthDate": "2020-03-03",
                    "gender": "MALE",
                    "preferredClassroomId": 1
                }
                """;

        MvcResult applyResult = mockMvc.perform(post("/api/v1/kid-applications")
                        .with(user(new CustomUserDetails(applicant)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isOk())
                .andReturn();

        long applicationId = objectMapper.readTree(applyResult.getResponse().getContentAsString())
                .path("data")
                .asLong();

        String approveBody = """
                {
                    "classroomId": 1
                }
                """;

        mockMvc.perform(put("/api/v1/kid-applications/{id}/approve", applicationId)
                        .with(user(new CustomUserDetails(applicant)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approveBody))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
