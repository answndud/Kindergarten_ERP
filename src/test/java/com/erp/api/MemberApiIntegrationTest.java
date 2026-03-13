package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.member.entity.MemberStatus;
import com.erp.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("회원 API 테스트")
class MemberApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원 탈퇴 - 모든 refresh 세션을 정리하고 현재 브라우저 쿠키를 만료시킨다")
    void withdraw_Success_RevokesAllRefreshSessions() throws Exception {
        LoginCookies firstLogin = loginAsParent();
        LoginCookies secondLogin = loginAsParent();

        String firstSessionKey = getRefreshSessionKey(firstLogin.refreshCookie());
        String secondSessionKey = getRefreshSessionKey(secondLogin.refreshCookie());
        String sessionSetKey = getRefreshSessionSetKey(firstLogin.refreshCookie());

        MvcResult withdrawResult = mockMvc.perform(delete("/api/v1/members/withdraw")
                        .with(csrf())
                        .cookie(firstLogin.accessCookie(), firstLogin.refreshCookie()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        assertThat(redisTemplate.opsForValue().get(firstSessionKey)).isNull();
        assertThat(redisTemplate.opsForValue().get(secondSessionKey)).isNull();
        assertThat(redisTemplate.hasKey(sessionSetKey)).isFalse();
        assertThat(memberRepository.findById(parentMember.getId()))
                .get()
                .extracting(member -> member.getStatus(), member -> member.getDeletedAt() != null)
                .containsExactly(MemberStatus.INACTIVE, true);
        assertThat(withdrawResult.getResponse().getCookie("access_token")).isNotNull();
        assertThat(withdrawResult.getResponse().getCookie("access_token").getMaxAge()).isZero();
        assertThat(withdrawResult.getResponse().getCookie("refresh_token")).isNotNull();
        assertThat(withdrawResult.getResponse().getCookie("refresh_token").getMaxAge()).isZero();
    }

    private LoginCookies loginAsParent() throws Exception {
        String loginBody = """
                {
                    "email": "parent@test.com",
                    "password": "test1234"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        return new LoginCookies(
                result.getResponse().getCookie("access_token"),
                result.getResponse().getCookie("refresh_token")
        );
    }

    private String getRefreshSessionKey(Cookie refreshCookie) {
        return "refresh:session:%d:%s".formatted(
                jwtTokenProvider.getMemberId(refreshCookie.getValue()),
                jwtTokenProvider.getSessionId(refreshCookie.getValue())
        );
    }

    private String getRefreshSessionSetKey(Cookie refreshCookie) {
        return "refresh:sessions:%d".formatted(jwtTokenProvider.getMemberId(refreshCookie.getValue()));
    }

    private record LoginCookies(Cookie accessCookie, Cookie refreshCookie) {
    }
}
