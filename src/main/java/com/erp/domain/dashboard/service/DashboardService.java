package com.erp.domain.dashboard.service;

import com.erp.domain.announcement.repository.AnnouncementRepository;
import com.erp.domain.attendance.repository.AttendanceRepository;
import com.erp.domain.dashboard.dto.response.DashboardStatisticsResponse;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kid.repository.KidRepository;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final MemberRepository memberRepository;
    private final KidRepository kidRepository;
    private final AttendanceRepository attendanceRepository;
    private final AnnouncementRepository announcementRepository;

    @Cacheable(cacheNames = "dashboardStatistics", key = "#kindergarten.id")
    public DashboardStatisticsResponse getDashboardStatistics(Kindergarten kindergarten) {
        long kindergartenId = kindergarten.getId();

        long totalKidsLong = kidRepository.countByClassroomKindergartenId(kindergartenId);
        int totalKids = (int) totalKidsLong;
        int totalTeachers = (int) memberRepository.countByKindergartenIdAndRole(kindergartenId, MemberRole.TEACHER);
        int totalParents = (int) memberRepository.countByKindergartenIdAndRole(kindergartenId, MemberRole.PARENT);

        double attendanceRate7Days = calculateAttendanceRate(kindergartenId, totalKidsLong, 7);
        double attendanceRate30Days = calculateAttendanceRate(kindergartenId, totalKidsLong, 30);
        double announcementReadRate = calculateAnnouncementReadRate(kindergartenId);

        int totalAnnouncements = (int) announcementRepository.countByKindergartenIdAndDeletedAtIsNull(kindergartenId);
        int todayAttendanceCount = (int) attendanceRepository.countByKidClassroomKindergartenIdAndDate(kindergartenId, LocalDate.now());

        return new DashboardStatisticsResponse(
                totalKids,
                totalTeachers,
                totalParents,
                attendanceRate7Days,
                attendanceRate30Days,
                announcementReadRate,
                totalAnnouncements,
                todayAttendanceCount
        );
    }

    @Transactional
    @CacheEvict(cacheNames = "dashboardStatistics", key = "#kindergartenId")
    public void evictDashboardStatisticsCache(Long kindergartenId) {
    }

    private double calculateAttendanceRate(long kindergartenId, long totalKids, int days) {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(days - 1);

        if (totalKids <= 0) {
            return 0.0;
        }

        long totalPossibleAttendance = totalKids * days;
        if (totalPossibleAttendance == 0) {
            return 0.0;
        }

        long totalAttendance = attendanceRepository.countPresentOrLateByKindergartenAndDateBetween(
                kindergartenId,
                startDate,
                endDate
        );

        return (totalAttendance * 100.0) / totalPossibleAttendance;
    }

    private double calculateAnnouncementReadRate(long kindergartenId) {
        long totalAnnouncements = announcementRepository.countByKindergartenIdAndDeletedAtIsNull(kindergartenId);
        if (totalAnnouncements == 0) {
            return 0.0;
        }

        long totalViewCount = announcementRepository.sumViewCountByKindergartenId(kindergartenId);

        long totalMembers = memberRepository.countByKindergartenIdAndDeletedAtIsNull(kindergartenId);
        if (totalMembers == 0 || totalViewCount == 0) {
            return 0.0;
        }

        return (totalViewCount * 100.0) / totalMembers;
    }
}
