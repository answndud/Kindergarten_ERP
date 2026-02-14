package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("알림 API 테스트")
class NotificationApiIntegrationTest extends BaseIntegrationTest {

    @Test
    @WithMockUser(username = "principal@test.com", roles = {"PRINCIPAL"})
    @DisplayName("알림 생성/전체 읽음 - 성공")
    void createAndReadAll_Success_Principal() throws Exception {
        String requestBody = """
                {
                    "receiverId": 1,
                    "type": "SYSTEM",
                    "title": "점검 안내",
                    "content": "오늘 저녁 시스템 점검이 예정되어 있습니다.",
                    "linkUrl": "/notifications"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(1));

        mockMvc.perform(put("/api/v1/notifications/read-all")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(0));
    }

    @Test
    @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
    @DisplayName("알림 생성 - 실패 (학부모 권한 없음)")
    void createNotification_Fail_Parent() throws Exception {
        String requestBody = """
                {
                    "receiverId": 1,
                    "type": "SYSTEM",
                    "title": "권한 테스트",
                    "content": "학부모는 생성 권한이 없습니다."
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
