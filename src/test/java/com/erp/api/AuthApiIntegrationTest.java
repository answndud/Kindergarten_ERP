package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import com.erp.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 인증 API 통합 테스트
 */
@DisplayName("인증 API 테스트")
class AuthApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void cleanupRedisState() {
        clearRedis();
    }

    @Test
    @DisplayName("인증되지 않은 API 요청 - 401 공통 응답 포맷 반환")
    void unauthorizedApiRequest_ReturnsStandardErrorResponse() throws Exception {
        clearAuthentication();

        mockMvc.perform(get("/api/v1/attendance/1"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("A001"));
    }

    @Nested
    @DisplayName("회원가입 API")
    class SignUpApiTest {

        @Test
        @DisplayName("회원가입 - 성공 (학부모)")
        void signup_Success_Parent() throws Exception {
            String requestBody = """
                    {
                        "email": "newparent@test.com",
                        "password": "Test1234!",
                        "passwordConfirm": "Test1234!",
                        "name": "새학부모",
                        "birthDate": "1990-01-01",
                        "phone": "01012345678",
                        "role": "PARENT"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("newparent@test.com"))
                    .andExpect(jsonPath("$.data.name").value("새학부모"));
        }

        @Test
        @DisplayName("회원가입 - 실패 (중복 이메일)")
        void signup_Fail_DuplicateEmail() throws Exception {
            String requestBody = """
                    {
                        "email": "parent@test.com",
                        "password": "Test1234!",
                        "passwordConfirm": "Test1234!",
                        "name": "중복학부모",
                        "birthDate": "1990-01-01",
                        "phone": "01012345678",
                        "role": "PARENT"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value("M001"));
        }

        @Test
        @DisplayName("회원가입 - 실패 (유효하지 않은 입력)")
        void signup_Fail_InvalidInput() throws Exception {
            String requestBody = """
                    {
                        "email": "invalid-email",
                        "password": "123",
                        "name": "",
                        "birthDate": "1990-01-01",
                        "phone": "010-1234-5678",
                        "role": "PARENT"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("로그인 API")
    class LoginApiTest {

        @Test
        @DisplayName("로그인 - 성공")
        void login_Success() throws Exception {
            String requestBody = """
                    {
                        "email": "parent@test.com",
                        "password": "test1234"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("로그인 - 실패 (잘못된 비밀번호)")
        void login_Fail_WrongPassword() throws Exception {
            String requestBody = """
                    {
                        "email": "parent@test.com",
                        "password": "wrongpassword"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value("A001"));
        }

        @Test
        @DisplayName("로그인 - 성공 (헤더 기반 CSRF 토큰)")
        void login_Success_WithHeaderCsrfToken() throws Exception {
            String requestBody = """
                    {
                        "email": "parent@test.com",
                        "password": "test1234"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf().asHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("로그인 - 실패 (존재하지 않는 이메일)")
        void login_Fail_EmailNotFound() throws Exception {
            String requestBody = """
                    {
                        "email": "notfound@test.com",
                        "password": "test1234"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value("A001"));
        }

        @Test
        @DisplayName("로그인 - 실패 (CSRF 토큰 없음)")
        void login_Fail_WithoutCsrfToken() throws Exception {
            String requestBody = """
                    {
                        "email": "parent@test.com",
                        "password": "test1234"
                    }
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("토큰 갱신 API")
    class TokenRefreshApiTest {

        @Test
        @DisplayName("토큰 갱신 - 성공 시 세션별 Redis 키에 저장되고 rotation 된다")
        void refreshToken_Success_WithRotation() throws Exception {
            LoginCookies loginCookies = loginAsParent();
            String sessionKey = getRefreshSessionKey(loginCookies.refreshCookie());
            String sessionSetKey = getRefreshSessionSetKey(loginCookies.refreshCookie());
            String originalSessionId = jwtTokenProvider.getSessionId(loginCookies.refreshCookie().getValue());

            assertNotNull(loginCookies.refreshCookie());
            assertThat(redisTemplate.opsForValue().get(sessionKey)).isEqualTo(loginCookies.refreshCookie().getValue());
            assertThat(redisTemplate.opsForSet().members(sessionSetKey))
                    .contains(originalSessionId);

            MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                            .with(csrf())
                            .cookie(loginCookies.refreshCookie()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            Cookie rotatedRefreshCookie = refreshResult.getResponse().getCookie("refresh_token");
            Cookie rotatedAccessCookie = refreshResult.getResponse().getCookie("access_token");

            assertNotNull(rotatedRefreshCookie);
            assertNotNull(rotatedAccessCookie);
            assertThat(rotatedRefreshCookie.getValue()).isNotEqualTo(loginCookies.refreshCookie().getValue());
            assertThat(jwtTokenProvider.getSessionId(rotatedRefreshCookie.getValue())).isEqualTo(originalSessionId);
            assertThat(redisTemplate.opsForValue().get(sessionKey)).isEqualTo(rotatedRefreshCookie.getValue());
        }

        @Test
        @DisplayName("토큰 갱신 - 실패 (Redis 토큰 불일치)")
        void refreshToken_Fail_TokenMismatch() throws Exception {
            LoginCookies loginCookies = loginAsParent();

            assertNotNull(loginCookies.refreshCookie());
            redisTemplate.opsForValue().set(getRefreshSessionKey(loginCookies.refreshCookie()), "mismatch-token");

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .with(csrf())
                            .cookie(loginCookies.refreshCookie()))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value("A005"));
        }

        @Test
        @DisplayName("로그아웃 - 현재 세션만 정리하고 다른 기기 세션은 유지한다")
        void logout_Success_RevokesOnlyCurrentSession() throws Exception {
            LoginCookies firstLogin = loginAsParent();
            LoginCookies secondLogin = loginAsParent();

            String firstSessionKey = getRefreshSessionKey(firstLogin.refreshCookie());
            String secondSessionKey = getRefreshSessionKey(secondLogin.refreshCookie());
            String sessionSetKey = getRefreshSessionSetKey(firstLogin.refreshCookie());
            String firstSessionId = jwtTokenProvider.getSessionId(firstLogin.refreshCookie().getValue());
            String secondSessionId = jwtTokenProvider.getSessionId(secondLogin.refreshCookie().getValue());

            assertThat(redisTemplate.opsForSet().members(sessionSetKey))
                    .contains(firstSessionId, secondSessionId);

            mockMvc.perform(post("/api/v1/auth/logout")
                            .with(csrf())
                            .cookie(firstLogin.accessCookie(), firstLogin.refreshCookie()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            assertThat(redisTemplate.opsForValue().get(firstSessionKey)).isNull();
            assertThat(redisTemplate.opsForValue().get(secondSessionKey)).isEqualTo(secondLogin.refreshCookie().getValue());
            assertThat(asStringSet(redisTemplate.opsForSet().members(sessionSetKey)))
                    .doesNotContain(firstSessionId)
                    .contains(secondSessionId);
        }
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

    private Set<String> asStringSet(Set<Object> values) {
        if (values == null) {
            return Set.of();
        }

        return values.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.toSet());
    }

    private record LoginCookies(Cookie accessCookie, Cookie refreshCookie) {
    }
}
