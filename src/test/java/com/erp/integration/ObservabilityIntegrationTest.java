package com.erp.integration;

import com.erp.common.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ObservabilityIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Actuator health는 인증 없이 조회할 수 있다")
    void actuatorHealth_IsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    @DisplayName("Actuator readiness probe는 활성화되어 있다")
    void actuatorReadinessProbe_IsEnabled() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("요청 correlation id를 응답 헤더로 그대로 돌려준다")
    void correlationIdHeader_IsEchoedBack() throws Exception {
        mockMvc.perform(get("/login")
                        .header("X-Correlation-Id", "demo-correlation-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "demo-correlation-123"));
    }
}
