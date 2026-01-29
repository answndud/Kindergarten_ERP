package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 인증 API 통합 테스트
 */
@DisplayName("인증 API 테스트")
class AuthApiIntegrationTest extends BaseIntegrationTest {

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
    }

    @Nested
    @DisplayName("토큰 갱신 API")
    class TokenRefreshApiTest {

        @Test
        @DisplayName("토큰 갱신 - 성공")
        void refreshToken_Success() throws Exception {
            // TODO: 리프레시 토큰 발급 후 갱신 테스트 구현
            // 현재는 기본 구조만 작성
        }
    }
}
