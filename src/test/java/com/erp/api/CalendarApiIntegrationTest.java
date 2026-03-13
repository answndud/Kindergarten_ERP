package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.calendar.entity.CalendarEvent;
import com.erp.domain.calendar.entity.CalendarEventType;
import com.erp.domain.calendar.entity.CalendarScopeType;
import com.erp.domain.calendar.entity.RepeatType;
import com.erp.domain.calendar.repository.CalendarEventRepository;
import com.erp.domain.classroom.entity.Classroom;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("캘린더 API 테스트")
class CalendarApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Test
    @DisplayName("반복 일정 조회 - 주간 반복 occurrence 확장 성공")
    void getRecurringClassroomEvents_Success() throws Exception {
        String requestBody = """
                {
                    "title": "주간 상담",
                    "description": "매주 상담 일정",
                    "startDateTime": "2026-03-03T10:00:00",
                    "endDateTime": "2026-03-03T11:00:00",
                    "eventType": "MEETING",
                    "scopeType": "CLASSROOM",
                    "classroomId": %d,
                    "isAllDay": false,
                    "repeatType": "WEEKLY",
                    "repeatEndDate": "2026-03-31"
                }
                """.formatted(classroom.getId());

        mockMvc.perform(post("/api/v1/calendar/events")
                        .with(authenticated(teacherMember))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.repeatType").value("WEEKLY"));

        mockMvc.perform(get("/api/v1/calendar/events")
                        .with(authenticated(teacherMember))
                        .param("startDate", "2026-03-17")
                        .param("endDate", "2026-03-17"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("주간 상담"))
                .andExpect(jsonPath("$.data[0].startDateTime").value("2026-03-17T10:00:00"))
                .andExpect(jsonPath("$.data[0].endDateTime").value("2026-03-17T11:00:00"))
                .andExpect(jsonPath("$.data[0].scopeType").value("CLASSROOM"));
    }

    @Test
    @DisplayName("유치원 전체 일정 조회 - 학부모 성공")
    void getKindergartenEvent_Success_Parent() throws Exception {
        CalendarEvent event = calendarEventRepository.save(CalendarEvent.create(
                kindergarten,
                null,
                principalMember,
                "봄 소풍 안내",
                "유치원 전체 일정",
                LocalDateTime.of(2026, 3, 20, 9, 0),
                LocalDateTime.of(2026, 3, 20, 12, 0),
                CalendarEventType.EVENT,
                CalendarScopeType.KINDERGARTEN,
                false,
                "한강공원",
                RepeatType.NONE,
                null
        ));

        mockMvc.perform(get("/api/v1/calendar/events")
                        .with(authenticated(parentMember))
                        .param("startDate", "2026-03-20")
                        .param("endDate", "2026-03-20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(event.getId()))
                .andExpect(jsonPath("$.data[0].scopeType").value("KINDERGARTEN"));

        mockMvc.perform(get("/api/v1/calendar/events/{id}", event.getId())
                        .with(authenticated(parentMember)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("봄 소풍 안내"));
    }

    @Test
    @DisplayName("일정 상세 조회 - 다른 유치원 일정은 차단")
    void getEvent_Fail_OtherKindergartenMember() throws Exception {
        Kindergarten otherKindergarten = testData.createKindergarten();
        Member otherPrincipal = createMemberInKindergarten(
                "other-principal@test.com",
                "다른 원장",
                MemberRole.PRINCIPAL,
                otherKindergarten
        );
        Classroom otherClassroom = testData.createClassroom(otherKindergarten);
        Member otherTeacher = createMemberInKindergarten(
                "other-teacher@test.com",
                "다른 교사",
                MemberRole.TEACHER,
                otherKindergarten
        );
        otherClassroom.assignTeacher(otherTeacher);
        classroomRepository.save(otherClassroom);

        CalendarEvent otherEvent = calendarEventRepository.save(CalendarEvent.create(
                otherKindergarten,
                null,
                otherPrincipal,
                "타 유치원 행사",
                "접근 차단 대상",
                LocalDateTime.of(2026, 3, 25, 10, 0),
                LocalDateTime.of(2026, 3, 25, 11, 0),
                CalendarEventType.EVENT,
                CalendarScopeType.KINDERGARTEN,
                false,
                null,
                RepeatType.NONE,
                null
        ));

        mockMvc.perform(get("/api/v1/calendar/events/{id}", otherEvent.getId())
                        .with(authenticated(teacherMember)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CA002"));
    }

    @Test
    @DisplayName("반복 일정 생성 - repeatEndDate 누락 시 실패")
    void createRecurringEvent_Fail_MissingRepeatEndDate() throws Exception {
        String requestBody = """
                {
                    "title": "주간 상담",
                    "description": "매주 상담 일정",
                    "startDateTime": "2026-03-03T10:00:00",
                    "endDateTime": "2026-03-03T11:00:00",
                    "eventType": "MEETING",
                    "scopeType": "CLASSROOM",
                    "classroomId": %d,
                    "isAllDay": false,
                    "repeatType": "WEEKLY"
                }
                """.formatted(classroom.getId());

        mockMvc.perform(post("/api/v1/calendar/events")
                        .with(authenticated(teacherMember))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("C001"));
    }
}
