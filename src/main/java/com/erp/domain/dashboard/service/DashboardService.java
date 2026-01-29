package com.erp.domain.dashboard.service;

import com.erp.domain.announcement.entity.Announcement;
import com.erp.domain.announcement.repository.AnnouncementRepository;
import com.erp.domain.attendance.entity.Attendance;
import com.erp.domain.attendance.entity.AttendanceStatus;
import com.erp.domain.attendance.repository.AttendanceRepository;
import com.erp.domain.dashboard.dto.response.DashboardStatisticsResponse;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.domain.kid.entity.Kid;
import com.erp.domain.kid.repository.KidRepository;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final MemberRepository memberRepository;
    private final KidRepository kidRepository;
    private final AttendanceRepository attendanceRepository;
    private final AnnouncementRepository announcementRepository;

    public DashboardStatisticsResponse getDashboardStatistics(Kindergarten kindergarten) {
        long kindergartenId = kindergarten.getId();

        int totalKids = (int) kidRepository.countByClassroomKindergartenId(kindergartenId);
        int totalTeachers = (int) memberRepository.countByKindergartenIdAndRole(kindergartenId, MemberRole.TEACHER);
        int totalParents = (int) memberRepository.countByKindergartenIdAndRole(kindergartenId, MemberRole.PARENT);

        double attendanceRate7Days = calculateAttendanceRate(kindergartenId, 7);
        double attendanceRate30Days = calculateAttendanceRate(kindergartenId, 30);
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

    private double calculateAttendanceRate(long kindergartenId, int days) {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Kid> kids = kidRepository.findByClassroomKindergartenId(kindergartenId);
        if (kids.isEmpty()) {
            return 0.0;
        }

        int totalPossibleAttendance = kids.size() * days;
        if (totalPossibleAttendance == 0) {
            return 0.0;
        }

        long totalPresent = attendanceRepository.countByKidClassroomKindergartenIdAndDateBetweenAndStatus(
                kindergartenId, startDate, endDate, AttendanceStatus.PRESENT);

        long totalLate = attendanceRepository.countByKidClassroomKindergartenIdAndDateBetweenAndStatus(
                kindergartenId, startDate, endDate, AttendanceStatus.LATE);

        long totalAttendance = totalPresent + totalLate;
        return (totalAttendance * 100.0) / totalPossibleAttendance;
    }

    private double calculateAnnouncementReadRate(long kindergartenId) {
        List<Announcement> announcements = announcementRepository.findByKindergartenIdAndDeletedAtIsNull(kindergartenId);
        if (announcements.isEmpty()) {
            return 0.0;
        }

        int totalViewCount = announcements.stream()
                .mapToInt(Announcement::getViewCount)
                .sum();

        int totalMembers = (int) memberRepository.countByKindergartenIdAndDeletedAtIsNull(kindergartenId);
        if (totalMembers == 0 || totalViewCount == 0) {
            return 0.0;
        }

        return (totalViewCount * 100.0) / totalMembers;
    }
}
