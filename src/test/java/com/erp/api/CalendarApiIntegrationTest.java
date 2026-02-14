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

@DisplayName("캘린더 API 테스트")
class CalendarApiIntegrationTest extends BaseIntegrationTest {

    @Test
    @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
    @DisplayName("일정 생성/조회 - 성공 (교사) ")
    void createAndGetEvents_Success_Teacher() throws Exception {
        String requestBody = """
                {
                    "title": "학부모 상담",
                    "description": "1학기 상담 주간",
                    "startDateTime": "2026-02-20T10:00:00",
                    "endDateTime": "2026-02-20T11:00:00",
                    "eventType": "MEETING",
                    "scopeType": "CLASSROOM",
                    "classroomId": 1,
                    "isAllDay": false,
                    "repeatType": "NONE"
                }
                """;

        mockMvc.perform(post("/api/v1/calendar/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("학부모 상담"))
                .andExpect(jsonPath("$.data.scopeType").value("CLASSROOM"));

        mockMvc.perform(get("/api/v1/calendar/events")
                        .param("startDate", "2026-02-01")
                        .param("endDate", "2026-02-28"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("학부모 상담"));
    }

    @Test
    @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
    @DisplayName("반 일정 생성 - 실패 (학부모 권한 없음)")
    void createClassroomEvent_Fail_Parent() throws Exception {
        String requestBody = """
                {
                    "title": "권한 없는 일정",
                    "description": "생성 불가",
                    "startDateTime": "2026-02-20T10:00:00",
                    "endDateTime": "2026-02-20T11:00:00",
                    "eventType": "EVENT",
                    "scopeType": "CLASSROOM",
                    "classroomId": 1,
                    "isAllDay": false,
                    "repeatType": "NONE"
                }
                """;

        mockMvc.perform(post("/api/v1/calendar/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
