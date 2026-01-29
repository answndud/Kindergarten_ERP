package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 알림장 API 통합 테스트
 */
@DisplayName("알림장 API 테스트")
class NotepadApiIntegrationTest extends BaseIntegrationTest {

    @Nested
    @DisplayName("알림장 생성 API")
    class CreateNotepadTest {

        @Test
        @WithMockUser(username = "principal@test.com", roles = {"PRINCIPAL"})
        @DisplayName("알림장 생성 - 성공 (원장)")
        void createNotepad_Success_Principal() throws Exception {
            String requestBody = """
                    {
                        "classroomId": 1,
                        "title": "오늘의 활동",
                        "content": "오늘 아이들이 미술 활동을 했습니다."
                    }
                    """;

            mockMvc.perform(post("/api/v1/notepads")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("오늘의 활동"));
        }

        @Test
        @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
        @DisplayName("알림장 생성 - 성공 (교사)")
        void createNotepad_Success_Teacher() throws Exception {
            String requestBody = """
                    {
                        "classroomId": 1,
                        "title": "오늘의 활동",
                        "content": "오늘 아이들이 미술 활동을 했습니다."
                    }
                    """;

            mockMvc.perform(post("/api/v1/notepads")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
        @DisplayName("알림장 생성 - 실패 (학부모는 권한 없음)")
        void createNotepad_Fail_Parent() throws Exception {
            String requestBody = """
                    {
                        "classroomId": 1,
                        "title": "오늘의 활동",
                        "content": "오늘 아이들이 미술 활동을 했습니다."
                    }
                    """;

            mockMvc.perform(post("/api/v1/notepads")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("알림장 생성 - 실패 (인증 없음)")
        void createNotepad_Fail_Unauthenticated() throws Exception {
            String requestBody = """
                    {
                        "classroomId": 1,
                        "title": "오늘의 활동",
                        "content": "오늘 아이들이 미술 활동을 했습니다."
                    }
                    """;

            mockMvc.perform(post("/api/v1/notepads")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("알림장 조회 API")
    class GetNotepadTest {

        @Test
        @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
        @DisplayName("알림장 단건 조회 - 성공")
        void getNotepad_Success() throws Exception {
            mockMvc.perform(get("/api/v1/notepads/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
        @DisplayName("반별 알림장 목록 조회 - 성공")
        void getClassroomNotepads_Success() throws Exception {
            mockMvc.perform(get("/api/v1/notepads/classroom/1")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
        @DisplayName("원생별 알림장 목록 조회 - 성공")
        void getKidNotepads_Success() throws Exception {
            mockMvc.perform(get("/api/v1/notepads/kid/1")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("알림장 수정/삭제 API")
    class UpdateDeleteNotepadTest {

        @Test
        @WithMockUser(username = "principal@test.com", roles = {"PRINCIPAL"})
        @DisplayName("알림장 수정 - 성공 (원장)")
        void updateNotepad_Success_Principal() throws Exception {
            String requestBody = """
                    {
                        "classroomId": 1,
                        "title": "수정된 제목",
                        "content": "수정된 내용"
                    }
                    """;

            mockMvc.perform(put("/api/v1/notepads/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "principal@test.com", roles = {"PRINCIPAL"})
        @DisplayName("알림장 삭제 - 성공 (원장)")
        void deleteNotepad_Success_Principal() throws Exception {
            mockMvc.perform(delete("/api/v1/notepads/1")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
        @DisplayName("알림장 삭제 - 실패 (학부모는 권한 없음)")
        void deleteNotepad_Fail_Parent() throws Exception {
            mockMvc.perform(delete("/api/v1/notepads/1")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("알림장 읽음 처리 API")
    class MarkAsReadTest {

        @Test
        @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
        @DisplayName("알림장 읽음 처리 - 성공")
        void markAsRead_Success() throws Exception {
            mockMvc.perform(post("/api/v1/notepads/1/read")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
