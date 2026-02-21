package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("유치원 API 테스트")
class KindergartenApiIntegrationTest extends BaseIntegrationTest {

    @Test
    @WithMockUser(username = "principal@test.com", roles = {"PRINCIPAL"})
    @DisplayName("유치원 등록 - 성공 (원장)")
    void createKindergarten_Success_Principal() throws Exception {
        String requestBody = """
                {
                    "name": "새 유치원",
                    "address": "서울시 강남구",
                    "phone": "01012345678",
                    "openTime": "09:00",
                    "closeTime": "18:00"
                }
                """;

        mockMvc.perform(post("/api/v1/kindergartens")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
    @DisplayName("유치원 등록 - 실패 (교사 권한 없음)")
    void createKindergarten_Fail_Teacher() throws Exception {
        String requestBody = """
                {
                    "name": "권한테스트 유치원",
                    "address": "서울시",
                    "phone": "01099998888",
                    "openTime": "09:00",
                    "closeTime": "18:00"
                }
                """;

        mockMvc.perform(post("/api/v1/kindergartens")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
    @DisplayName("유치원 목록 조회 - 성공")
    void getKindergartens_Success() throws Exception {
        mockMvc.perform(get("/api/v1/kindergartens"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
