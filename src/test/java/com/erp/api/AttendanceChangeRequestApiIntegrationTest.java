package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.attendance.entity.AttendanceChangeRequestStatus;
import com.erp.domain.attendance.entity.AttendanceStatus;
import com.erp.domain.attendance.repository.AttendanceChangeRequestRepository;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.member.entity.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("출결 변경 요청 API 테스트")
class AttendanceChangeRequestApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AttendanceChangeRequestRepository attendanceChangeRequestRepository;

    @Test
    @DisplayName("학부모는 자기 자녀의 출결 변경 요청을 생성할 수 있다")
    void createAttendanceChangeRequest_Success() throws Exception {
        String requestBody = """
                {
                    "kidId": 1,
                    "date": "2025-01-14",
                    "status": "ABSENT",
                    "note": "병원 진료"
                }
                """;

        mockMvc.perform(post("/api/v1/attendance-requests")
                        .with(authenticated(parentMember))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        var requests = attendanceChangeRequestRepository.findByRequesterIdOrderByCreatedAtDesc(parentMember.getId());
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getRequestedStatus()).isEqualTo(AttendanceStatus.ABSENT);
        assertThat(requests.get(0).getStatus()).isEqualTo(AttendanceChangeRequestStatus.PENDING);
    }

    @Test
    @DisplayName("학부모는 다른 유치원 원생의 출결 변경 요청을 생성할 수 없다")
    void createAttendanceChangeRequest_Fail_DifferentKindergartenKid() throws Exception {
        Kindergarten otherKindergarten = testData.createKindergarten();
        var otherTeacher = createMemberInKindergarten(
                "attendance-request-other-teacher@test.com",
                "다른교사",
                MemberRole.TEACHER,
                otherKindergarten
        );
        var otherClassroom = testData.createClassroom(otherKindergarten);
        otherClassroom.assignTeacher(otherTeacher);
        classroomRepository.saveAndFlush(otherClassroom);
        var otherKid = testData.createKid(otherClassroom);

        String requestBody = """
                {
                    "kidId": %d,
                    "date": "2025-01-14",
                    "status": "ABSENT",
                    "note": "외부 원생"
                }
                """.formatted(otherKid.getId());

        mockMvc.perform(post("/api/v1/attendance-requests")
                        .with(authenticated(parentMember))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AT007"));
    }

    @Test
    @DisplayName("교사는 대기 중인 출결 변경 요청을 승인할 수 있다")
    void approveAttendanceChangeRequest_Success() throws Exception {
        long requestId = createAttendanceChangeRequest();

        mockMvc.perform(post("/api/v1/attendance-requests/{id}/approve", requestId)
                        .with(authenticated(teacherMember))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        var request = attendanceChangeRequestRepository.findById(requestId).orElseThrow();
        assertThat(request.getStatus()).isEqualTo(AttendanceChangeRequestStatus.APPROVED);
        assertThat(request.getAttendanceId()).isNotNull();
        assertThat(attendanceRepository.findByKidIdAndDate(kid.getId(), request.getDate()).orElseThrow().getStatus())
                .isEqualTo(AttendanceStatus.ABSENT);
    }

    @Test
    @DisplayName("교사는 대기 중인 출결 변경 요청을 거절할 수 있다")
    void rejectAttendanceChangeRequest_Success() throws Exception {
        long requestId = createAttendanceChangeRequest();

        mockMvc.perform(post("/api/v1/attendance-requests/{id}/reject", requestId)
                        .with(authenticated(teacherMember))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "reason": "증빙 서류가 필요합니다"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        var request = attendanceChangeRequestRepository.findById(requestId).orElseThrow();
        assertThat(request.getStatus()).isEqualTo(AttendanceChangeRequestStatus.REJECTED);
        assertThat(request.getRejectionReason()).isEqualTo("증빙 서류가 필요합니다");
        assertThat(attendanceRepository.findByKidIdAndDate(kid.getId(), request.getDate())).isEmpty();
    }

    @Test
    @DisplayName("다른 유치원 교사는 출결 요청을 승인할 수 없다")
    void approveAttendanceChangeRequest_Fail_DifferentKindergartenTeacher() throws Exception {
        long requestId = createAttendanceChangeRequest();

        Kindergarten otherKindergarten = testData.createKindergarten();
        var otherTeacher = createMemberInKindergarten(
                "attendance-request-reviewer@test.com",
                "외부교사",
                MemberRole.TEACHER,
                otherKindergarten
        );

        mockMvc.perform(post("/api/v1/attendance-requests/{id}/approve", requestId)
                        .with(authenticated(otherTeacher))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AP007"));
    }

    private long createAttendanceChangeRequest() throws Exception {
        String requestBody = """
                {
                    "kidId": 1,
                    "date": "2025-01-14",
                    "status": "ABSENT",
                    "note": "병원 진료"
                }
                """;

        String response = mockMvc.perform(post("/api/v1/attendance-requests")
                        .with(authenticated(parentMember))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").asLong();
    }
}
