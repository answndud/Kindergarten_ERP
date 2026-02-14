package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("대시보드 API 테스트")
class DashboardApiIntegrationTest extends BaseIntegrationTest {

    @Test
    @WithMockUser(username = "principal@test.com", roles = {"PRINCIPAL"})
    @DisplayName("대시보드 통계 조회 - 성공 (원장)")
    void getDashboardStatistics_Success_Principal() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalKids").isNumber())
                .andExpect(jsonPath("$.data.totalTeachers").isNumber())
                .andExpect(jsonPath("$.data.totalParents").isNumber())
                .andExpect(jsonPath("$.data.attendanceRate7Days").isNumber());
    }

    @Test
    @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
    @DisplayName("대시보드 통계 조회 - 실패 (학부모는 권한 없음)")
    void getDashboardStatistics_Fail_Parent() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/statistics"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
