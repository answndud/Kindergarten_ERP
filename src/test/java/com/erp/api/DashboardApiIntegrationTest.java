package com.erp.api;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.attendance.entity.Attendance;
import com.erp.domain.attendance.entity.AttendanceStatus;
import com.erp.domain.dashboard.service.DashboardService;
import com.erp.domain.kid.entity.Gender;
import com.erp.domain.kid.entity.Kid;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("대시보드 API 테스트")
@Tag("integration")
class DashboardApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @AfterEach
    void clearDashboardCache() {
        dashboardService.evictDashboardStatisticsCache(kindergarten.getId());
    }

    @Test
    @DisplayName("대시보드 통계 조회 - 성공 (원장)")
    void getDashboardStatistics_Success_Principal() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/statistics")
                        .with(authenticated(principalMember)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalKids").isNumber())
                .andExpect(jsonPath("$.data.totalTeachers").isNumber())
                .andExpect(jsonPath("$.data.totalParents").isNumber())
                .andExpect(jsonPath("$.data.attendanceRate7Days").isNumber());
    }

    @Test
    @DisplayName("대시보드 통계 조회 - 실패 (학부모는 권한 없음)")
    void getDashboardStatistics_Fail_Parent() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/statistics")
                        .with(authenticated(parentMember)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("대시보드 공지 열람률 - 중복 조회가 아닌 고유 열람 기준으로 집계한다")
    void getDashboardStatistics_UsesUniqueAnnouncementReaders() throws Exception {
        mockMvc.perform(get("/api/v1/announcements/{id}", announcement.getId())
                        .with(authenticated(parentMember)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/announcements/{id}", announcement.getId())
                        .with(authenticated(parentMember)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/announcements/{id}", announcement.getId())
                        .with(authenticated(teacherMember)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/dashboard/statistics")
                        .with(authenticated(principalMember)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAnnouncements").value(1))
                .andExpect(jsonPath("$.data.announcementReadRate").value(closeTo(66.666, 0.2)));
    }

    @Test
    @DisplayName("대시보드 출석률 - 주말과 입소 전 기간을 분모에서 제외한다")
    void getDashboardStatistics_AttendanceRate_ExcludesWeekendAndPreAdmissionDays() throws Exception {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(6);
        List<LocalDate> schoolDays = schoolDaysBetween(startDate, endDate);

        Kid earlyKid = kidRepository.save(Kid.create(
                classroom,
                "조기 입소 원생",
                LocalDate.of(2020, 2, 1),
                Gender.FEMALE,
                startDate.minusDays(3)
        ));
        LocalDate lateAdmissionDate = schoolDays.get(Math.max(schoolDays.size() - 2, 0));
        Kid lateKid = kidRepository.save(Kid.create(
                classroom,
                "후기 입소 원생",
                LocalDate.of(2020, 3, 1),
                Gender.MALE,
                lateAdmissionDate
        ));

        for (LocalDate schoolDay : schoolDays) {
            attendanceRepository.save(Attendance.create(earlyKid, schoolDay, AttendanceStatus.PRESENT));
            if (!schoolDay.isBefore(lateAdmissionDate)) {
                attendanceRepository.save(Attendance.create(lateKid, schoolDay, AttendanceStatus.PRESENT));
            }
        }

        dashboardService.evictDashboardStatisticsCache(kindergarten.getId());

        mockMvc.perform(get("/api/v1/dashboard/statistics")
                        .with(authenticated(principalMember)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceRate7Days").value(closeTo(100.0, 0.01)));
    }

    private List<LocalDate> schoolDaysBetween(LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .toList();
    }
}
