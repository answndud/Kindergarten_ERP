package com.erp.integration;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.authaudit.service.AuthAuditLogService;
import com.erp.domain.member.entity.MemberAuthProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ObservabilityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthAuditLogService authAuditLogService;

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
    @DisplayName("Prometheus endpoint는 인증 없이 조회할 수 있고 auth event metric을 노출한다")
    void actuatorPrometheus_IsPublic_AndExposesAuthMetric() throws Exception {
        authAuditLogService.recordLoginSuccess(
                principalMember.getId(),
                principalMember.getEmail(),
                MemberAuthProvider.LOCAL,
                "198.51.100.10"
        );

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("erp_auth_events_total")))
                .andExpect(content().string(containsString("event_type=\"login\"")))
                .andExpect(content().string(containsString("result=\"success\"")))
                .andExpect(content().string(containsString("provider=\"local\"")));
    }

    @Test
    @DisplayName("Swagger UI와 OpenAPI 문서는 인증 없이 조회할 수 있다")
    void swaggerUi_AndApiDocs_ArePublic() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Kindergarten ERP API"))
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.type").value("apiKey"))
                .andExpect(jsonPath("$.paths").exists());
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
