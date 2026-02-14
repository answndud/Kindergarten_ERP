package com.erp.performance;

import com.erp.common.BaseIntegrationTest;
import com.erp.domain.announcement.entity.Announcement;
import com.erp.domain.attendance.entity.AttendanceStatus;
import com.erp.domain.dashboard.service.DashboardService;
import com.erp.domain.kid.entity.Kid;
import com.erp.domain.member.entity.MemberRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("성능 스토리 - 대시보드 통계")
class DashboardPerformanceStoryTest extends BaseIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("통계 집계 경로 - 레거시 대비 쿼리 수/응답시간 비교")
    void compareLegacyVsOptimizedDashboardStatistics() {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        // warm-up
        legacyDashboardStatistics(kindergarten.getId());
        dashboardService.getDashboardStatistics(kindergarten);

        Measurement legacy = measure(statistics, () -> legacyDashboardStatistics(kindergarten.getId()));
        Measurement optimized = measure(statistics, () -> dashboardService.getDashboardStatistics(kindergarten));

        System.out.printf("[PERF] dashboard-legacy    - queries=%d, elapsedMs=%d%n", legacy.queryCount, legacy.elapsedMs);
        System.out.printf("[PERF] dashboard-optimized - queries=%d, elapsedMs=%d%n", optimized.queryCount, optimized.elapsedMs);

        assertTrue(optimized.queryCount < legacy.queryCount,
                "optimized dashboard path must use fewer queries than legacy path");
    }

    private void legacyDashboardStatistics(Long kindergartenId) {
        kidRepository.countByClassroomKindergartenId(kindergartenId);
        memberRepository.countByKindergartenIdAndRole(kindergartenId, MemberRole.TEACHER);
        memberRepository.countByKindergartenIdAndRole(kindergartenId, MemberRole.PARENT);

        legacyAttendanceRate(kindergartenId, 7);
        legacyAttendanceRate(kindergartenId, 30);
        legacyAnnouncementReadRate(kindergartenId);

        announcementRepository.countByKindergartenIdAndDeletedAtIsNull(kindergartenId);
        attendanceRepository.countByKidClassroomKindergartenIdAndDate(kindergartenId, LocalDate.now());
    }

    private double legacyAttendanceRate(Long kindergartenId, int days) {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Kid> kids = kidRepository.findByClassroomKindergartenId(kindergartenId);
        if (kids.isEmpty()) {
            return 0.0;
        }

        long totalPossibleAttendance = (long) kids.size() * days;
        if (totalPossibleAttendance == 0) {
            return 0.0;
        }

        long totalPresent = attendanceRepository.countByKidClassroomKindergartenIdAndDateBetweenAndStatus(
                kindergartenId,
                startDate,
                endDate,
                AttendanceStatus.PRESENT
        );
        long totalLate = attendanceRepository.countByKidClassroomKindergartenIdAndDateBetweenAndStatus(
                kindergartenId,
                startDate,
                endDate,
                AttendanceStatus.LATE
        );

        return ((totalPresent + totalLate) * 100.0) / totalPossibleAttendance;
    }

    private double legacyAnnouncementReadRate(Long kindergartenId) {
        List<Announcement> announcements = announcementRepository.findByKindergartenIdAndDeletedAtIsNull(kindergartenId);
        if (announcements.isEmpty()) {
            return 0.0;
        }

        int totalViewCount = announcements.stream()
                .mapToInt(Announcement::getViewCount)
                .sum();
        long totalMembers = memberRepository.countByKindergartenIdAndDeletedAtIsNull(kindergartenId);
        if (totalMembers == 0 || totalViewCount == 0) {
            return 0.0;
        }

        return (totalViewCount * 100.0) / totalMembers;
    }

    private Measurement measure(Statistics statistics, Runnable action) {
        entityManager.clear();
        statistics.clear();
        long start = System.nanoTime();
        action.run();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        long queryCount = statistics.getPrepareStatementCount();
        return new Measurement(queryCount, elapsedMs);
    }

    private record Measurement(long queryCount, long elapsedMs) {
    }
}
