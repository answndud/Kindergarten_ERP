package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.classroom.entity.Classroom;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kid.entity.Kid;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.kindergarten.repository.KindergartenRepository;
import com.erp.domain.classroom.repository.ClassroomRepository;
import com.erp.domain.kid.repository.KidRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("원생 API 테스트")
class KidApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KindergartenRepository kindergartenRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private KidRepository kidRepository;

    @AfterEach
    void cleanUp() {
        testData.cleanup();
    }

    @Nested
    @DisplayName("원생 생성 API")
    class CreateKidTest {

        @Test
        @WithMockUser(username = "principal@test.com", roles = {"PRINCIPAL"})
        @DisplayName("원생 생성 - 성공 (원장)")
        void createKid_Success_Principal() throws Exception {
            Kindergarten kg = testData.createKindergarten();
            Classroom cr = testData.createClassroom(kg);

            String requestBody = String.format("""
                    {
                        "classroomId": %d,
                        "name": "테스트 원생",
                        "birthDate": "2020-01-01",
                        "gender": "MALE",
                        "admissionDate": "%s"
                    }
                    """, cr.getId(), LocalDate.now());

            mockMvc.perform(post("/api/v1/kids")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("테스트 원생"));
        }

        @Test
        @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
        @DisplayName("원생 생성 - 성공 (교사)")
        void createKid_Success_Teacher() throws Exception {
            String requestBody = String.format("""
                    {
                        "classroomId": 1,
                        "name": "테스트 원생",
                        "birthDate": "2020-01-01",
                        "gender": "FEMALE",
                        "admissionDate": "%s"
                    }
                    """, LocalDate.now());

            mockMvc.perform(post("/api/v1/kids")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
        @DisplayName("원생 생성 - 실패 (학부모는 권한 없음)")
        void createKid_Fail_Parent() throws Exception {
            String requestBody = String.format("""
                    {
                        "classroomId": 1,
                        "name": "테스트 원생",
                        "birthDate": "2020-01-01",
                        "gender": "MALE",
                        "admissionDate": "%s"
                    }
                    """, LocalDate.now());

            mockMvc.perform(post("/api/v1/kids")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("원생 조회 API")
    class GetKidTest {

        @Test
        @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
        @DisplayName("원생 단건 조회 - 성공")
        void getKid_Success() throws Exception {
            mockMvc.perform(get("/api/v1/kids/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
        @DisplayName("반별 원생 목록 조회 - 성공")
        void getKidsByClassroom_Success() throws Exception {
            mockMvc.perform(get("/api/v1/kids")
                            .param("classroomId", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
        @DisplayName("반별 원생 이름 검색 - 성공")
        void searchKidsByName_Success() throws Exception {
            mockMvc.perform(get("/api/v1/kids")
                            .param("classroomId", "1")
                            .param("name", "테스트"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
        @DisplayName("원생 목록 조회 - 실패 (필터 누락)")
        void getKids_Fail_WhenFilterMissing() throws Exception {
            mockMvc.perform(get("/api/v1/kids"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value("C001"));
        }

        @Test
        @WithMockUser(username = "parent@test.com", roles = {"PARENT"})
        @DisplayName("학부모의 원생 목록 조회 - 성공")
        void getMyKids_Success() throws Exception {
            mockMvc.perform(get("/api/v1/kids/my-kids"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("원생 수정 API")
    class UpdateKidTest {

        @Test
        @WithMockUser(username = "principal@test.com", roles = {"PRINCIPAL"})
        @DisplayName("원생 정보 수정 - 성공 (원장)")
        void updateKid_Success_Principal() throws Exception {
            String requestBody = String.format("""
                    {
                        "classroomId": 1,
                        "name": "수정된 이름",
                        "birthDate": "2020-01-01",
                        "gender": "MALE",
                        "admissionDate": "%s"
                    }
                    """, LocalDate.now());

            mockMvc.perform(put("/api/v1/kids/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("수정된 이름"));
        }

        @Test
        @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
        @DisplayName("원생 정보 수정 - 성공 (교사)")
        void updateKid_Success_Teacher() throws Exception {
            String requestBody = String.format("""
                    {
                        "classroomId": 1,
                        "name": "수정된 이름",
                        "birthDate": "2020-01-01",
                        "gender": "FEMALE",
                        "admissionDate": "%s"
                    }
                    """, LocalDate.now());

            mockMvc.perform(put("/api/v1/kids/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("반 배정 변경 API")
    class UpdateClassroomTest {

        @Test
        @WithMockUser(username = "principal@test.com", roles = {"PRINCIPAL"})
        @DisplayName("반 배정 변경 - 성공 (원장)")
        void updateClassroom_Success_Principal() throws Exception {
            String requestBody = """
                    {
                        "classroomId": 1
                    }
                    """;

            mockMvc.perform(put("/api/v1/kids/1/classroom")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
        @DisplayName("반 배정 변경 - 실패 (교사는 권한 없음)")
        void updateClassroom_Fail_Teacher() throws Exception {
            String requestBody = """
                    {
                        "classroomId": 1
                    }
                    """;

            mockMvc.perform(put("/api/v1/kids/1/classroom")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("학부모 연결 API")
    class AssignParentTest {

        @Test
        @WithMockUser(username = "principal@test.com", roles = {"PRINCIPAL"})
        @DisplayName("학부모 연결 - 성공")
        void assignParent_Success() throws Exception {
            Member extraParent = testData.createTestMember("parent2@test.com", "추가 학부모", MemberRole.PARENT, "test1234");

            String requestBody = String.format("""
                    {
                        "parentId": %d,
                        "relationship": "FATHER"
                    }
                    """, extraParent.getId());

            mockMvc.perform(post("/api/v1/kids/1/parents")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
        @DisplayName("학부모 연결 - 실패 (교사는 권한 없음)")
        void assignParent_Fail_Teacher() throws Exception {
            String requestBody = """
                    {
                        "parentId": 3,
                        "relationship": "FATHER"
                    }
                    """;

            mockMvc.perform(post("/api/v1/kids/1/parents")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("학부모 연결 해제 API")
    class RemoveParentTest {

        @Test
        @WithMockUser(username = "principal@test.com", roles = {"PRINCIPAL"})
        @DisplayName("학부모 연결 해제 - 성공")
        void removeParent_Success() throws Exception {
            mockMvc.perform(delete("/api/v1/kids/1/parents/3")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
        @DisplayName("학부모 연결 해제 - 실패 (교사는 권한 없음)")
        void removeParent_Fail_Teacher() throws Exception {
            mockMvc.perform(delete("/api/v1/kids/1/parents/3")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("원생 삭제 API")
    class DeleteKidTest {

        @Test
        @WithMockUser(username = "principal@test.com", roles = {"PRINCIPAL"})
        @DisplayName("원생 삭제 - 성공 (원장)")
        void deleteKid_Success_Principal() throws Exception {
            mockMvc.perform(delete("/api/v1/kids/1")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(username = "teacher@test.com", roles = {"TEACHER"})
        @DisplayName("원생 삭제 - 실패 (교사는 권한 없음)")
        void deleteKid_Fail_Teacher() throws Exception {
            mockMvc.perform(delete("/api/v1/kids/1")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
