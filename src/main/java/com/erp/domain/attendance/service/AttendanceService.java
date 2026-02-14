package com.erp.domain.attendance.service;

import com.erp.domain.attendance.dto.request.AttendanceRequest;
import com.erp.domain.attendance.dto.request.BulkAttendanceRequest;
import com.erp.domain.attendance.dto.request.DropOffRequest;
import com.erp.domain.attendance.dto.request.PickUpRequest;
import com.erp.domain.attendance.dto.response.AttendanceResponse;
import com.erp.domain.attendance.dto.response.DailyAttendanceResponse;
import com.erp.domain.attendance.dto.response.MonthlyAttendanceKidReportResponse;
import com.erp.domain.attendance.dto.response.MonthlyAttendanceReportResponse;
import com.erp.domain.attendance.dto.response.MonthlyStatisticsResponse;
import com.erp.domain.attendance.entity.Attendance;
import com.erp.domain.attendance.entity.AttendanceStatus;
import com.erp.domain.attendance.repository.AttendanceRepository;
import com.erp.domain.classroom.service.ClassroomService;
import com.erp.domain.dashboard.service.DashboardService;
import com.erp.domain.kid.service.KidService;
import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 출석 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final KidService kidService;
    private final ClassroomService classroomService;
    private final DashboardService dashboardService;

    /**
     * 출석 등록
     */
    @Transactional
    public Long createAttendance(AttendanceRequest request) {
        // 원생 조회
        kidService.getKid(request.getKidId());

        // 중복 확인
        Attendance attendance = attendanceRepository.findByKidIdAndDate(request.getKidId(), request.getDate())
                .orElse(null);

        if (attendance == null) {
            attendance = Attendance.create(
                    kidService.getKid(request.getKidId()),
                    request.getDate(),
                    request.getStatus()
            );
        }

        applyStatus(attendance,
                request.getStatus(),
                request.getNote(),
                request.getDropOffTime(),
                request.getPickUpTime());

        Attendance saved = attendanceRepository.save(attendance);
        evictDashboardStatisticsByAttendance(saved);
        return saved.getId();
    }

    /**
     * 출석 조회
     */
    public Attendance getAttendance(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));
    }

    /**
     * 원생별 날짜 출석 조회
     */
    public Attendance getAttendanceByKidAndDate(Long kidId, LocalDate date) {
        // 원생 존재 확인
        kidService.getKid(kidId);

        return attendanceRepository.findByKidIdAndDate(kidId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));
    }

    /**
     * 반별 날짜 출석 목록 조회
     */
    public List<Attendance> getAttendancesByClassroomAndDate(Long classroomId, LocalDate date) {
        // 반 존재 확인
        classroomService.getClassroom(classroomId);

        return attendanceRepository.findByClassroomIdAndDate(classroomId, date);
    }

    /**
     * 원생별 월간 출석 목록 조회
     */
    public List<Attendance> getAttendancesByKidAndMonth(Long kidId, int year, int month) {
        // 원생 존재 확인
        kidService.getKid(kidId);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return attendanceRepository.findByKidIdAndDateBetween(kidId, startDate, endDate);
    }

    /**
     * 출석 수정
     */
    @Transactional
    public void updateAttendance(Long id, AttendanceRequest request) {
        Attendance attendance = getAttendance(id);
        applyStatus(attendance,
                request.getStatus(),
                request.getNote(),
                request.getDropOffTime(),
                request.getPickUpTime());
        evictDashboardStatisticsByAttendance(attendance);
    }

    /**
     * 출석 등록/수정 (Upsert)
     */
    @Transactional
    public AttendanceResponse upsertAttendance(AttendanceRequest request) {
        Attendance attendance = attendanceRepository.findByKidIdAndDate(request.getKidId(), request.getDate())
                .orElseGet(() -> Attendance.create(kidService.getKid(request.getKidId()), request.getDate(), request.getStatus()));

        applyStatus(attendance,
                request.getStatus(),
                request.getNote(),
                request.getDropOffTime(),
                request.getPickUpTime());

        Attendance saved = attendanceRepository.save(attendance);
        evictDashboardStatisticsByAttendance(saved);
        return AttendanceResponse.from(saved);
    }

    /**
     * 반별 일괄 출석 처리
     */
    @Transactional
    public int bulkUpdateAttendance(BulkAttendanceRequest request) {
        List<Long> kidIds;
        if (request.getKidIds() == null || request.getKidIds().isEmpty()) {
            kidIds = kidService.getKidsByClassroom(request.getClassroomId()).stream()
                    .map(com.erp.domain.kid.entity.Kid::getId)
                    .toList();
        } else {
            kidIds = request.getKidIds();
        }

        int updated = 0;
        for (Long kidId : kidIds) {
            Attendance attendance = attendanceRepository.findByKidIdAndDate(kidId, request.getDate())
                    .orElseGet(() -> Attendance.create(kidService.getKid(kidId), request.getDate(), request.getStatus()));

            applyStatus(attendance,
                    request.getStatus(),
                    request.getNote(),
                    request.getDropOffTime(),
                    request.getPickUpTime());

            attendanceRepository.save(attendance);
            evictDashboardStatisticsByAttendance(attendance);
            updated++;
        }

        return updated;
    }

    /**
     * 등원 기록
     */
    @Transactional
    public void recordDropOff(Long kidId, LocalDate date, DropOffRequest request) {
        // 원생 존재 확인
        kidService.getKid(kidId);

        java.time.LocalTime dropOffTime = request.getDropOffTime() != null
                ? request.getDropOffTime()
                : java.time.LocalTime.now();

        // 기존 출석 확인
        Attendance attendance = attendanceRepository.findByKidIdAndDate(kidId, date)
                .orElseGet(() -> {
                    // 없으면 새로 생성
                    Attendance newAttendance = Attendance.createDropOff(
                            kidService.getKid(kidId),
                            date,
                            dropOffTime
                    );
                    return attendanceRepository.save(newAttendance);
                });

        attendance.recordDropOff(dropOffTime);
        evictDashboardStatisticsByAttendance(attendance);
    }

    /**
     * 하원 기록
     */
    @Transactional
    public void recordPickUp(Long kidId, LocalDate date, PickUpRequest request) {
        // 원생 존재 확인
        kidService.getKid(kidId);

        java.time.LocalTime pickUpTime = request.getPickUpTime() != null
                ? request.getPickUpTime()
                : java.time.LocalTime.now();

        Attendance attendance = attendanceRepository.findByKidIdAndDate(kidId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));

        attendance.recordPickUp(pickUpTime);
        evictDashboardStatisticsByAttendance(attendance);
    }

    /**
     * 결석 처리
     */
    @Transactional
    public void markAbsent(Long kidId, LocalDate date, String note) {
        Attendance attendance = attendanceRepository.findByKidIdAndDate(kidId, date)
                .orElseGet(() -> {
                    // 없으면 새로 생성
                    Attendance newAttendance = Attendance.create(
                            kidService.getKid(kidId),
                            date,
                            AttendanceStatus.ABSENT
                    );
                    return attendanceRepository.save(newAttendance);
                });

        attendance.markAbsent(note);
        evictDashboardStatisticsByAttendance(attendance);
    }

    /**
     * 지각 처리
     */
    @Transactional
    public void markLate(Long kidId, LocalDate date, java.time.LocalTime dropOffTime, String note) {
        Attendance attendance = attendanceRepository.findByKidIdAndDate(kidId, date)
                .orElseGet(() -> {
                    // 없으면 새로 생성
                    Attendance newAttendance = Attendance.create(
                            kidService.getKid(kidId),
                            date,
                            AttendanceStatus.LATE
                    );
                    return attendanceRepository.save(newAttendance);
                });

        attendance.markLate(dropOffTime, note);
        evictDashboardStatisticsByAttendance(attendance);
    }

    /**
     * 조퇴 처리
     */
    @Transactional
    public void markEarlyLeave(Long kidId, LocalDate date, java.time.LocalTime pickUpTime, String note) {
        Attendance attendance = attendanceRepository.findByKidIdAndDate(kidId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));

        attendance.markEarlyLeave(pickUpTime, note);
        evictDashboardStatisticsByAttendance(attendance);
    }

    /**
     * 병결 처리
     */
    @Transactional
    public void markSickLeave(Long kidId, LocalDate date, String note) {
        Attendance attendance = attendanceRepository.findByKidIdAndDate(kidId, date)
                .orElseGet(() -> {
                    // 없으면 새로 생성
                    Attendance newAttendance = Attendance.create(
                            kidService.getKid(kidId),
                            date,
                            AttendanceStatus.SICK_LEAVE
                    );
                    return attendanceRepository.save(newAttendance);
                });

        attendance.markSickLeave(note);
        evictDashboardStatisticsByAttendance(attendance);
    }

    /**
     * 출석 삭제
     */
    @Transactional
    public void deleteAttendance(Long id) {
        Attendance attendance = getAttendance(id);
        evictDashboardStatisticsByAttendance(attendance);
        attendanceRepository.delete(attendance);
    }

    /**
     * 월간 출석 통계 조회
     */
    public MonthlyStatisticsResponse getMonthlyStatistics(Long kidId, int year, int month) {
        // 원생 조회
        var kid = kidService.getKid(kidId);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 출석일수 (지각 포함)
        long presentDays = attendanceRepository.countPresentDaysByKidIdAndDateBetween(kidId, startDate, endDate);

        // 결석일수 (병결 포함)
        long absentDays = attendanceRepository.countAbsentDaysByKidIdAndDateBetween(kidId, startDate, endDate);

        // 지각일수
        long lateDays = attendanceRepository.countByKidIdAndDateBetweenAndStatus(
                kidId, startDate, endDate, AttendanceStatus.LATE);

        // 병결일수
        long sickLeaveDays = attendanceRepository.countByKidIdAndDateBetweenAndStatus(
                kidId, startDate, endDate, AttendanceStatus.SICK_LEAVE);

        // 전체 등록일수
        List<Attendance> allAttendances = attendanceRepository.findByKidIdAndDateBetween(kidId, startDate, endDate);
        int totalDays = allAttendances.size();

        return new MonthlyStatisticsResponse(
                kidId,
                kid.getName(),
                year,
                month,
                (int) presentDays,
                (int) absentDays,
                (int) lateDays,
                (int) sickLeaveDays,
                totalDays
        );
    }

    /**
     * 일별 출석 현황 (반별)
     */
    public List<DailyAttendanceResponse> getDailyAttendanceByClassroom(Long classroomId, LocalDate date) {
        List<com.erp.domain.kid.entity.Kid> kids = kidService.getKidsByClassroom(classroomId);
        List<Attendance> attendances = getAttendancesByClassroomAndDate(classroomId, date);
        java.util.Map<Long, Attendance> attendanceMap = attendances.stream()
                .collect(java.util.stream.Collectors.toMap(a -> a.getKid().getId(), a -> a));

        return kids.stream()
                .map(kid -> DailyAttendanceResponse.from(kid, attendanceMap.get(kid.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 반별 월간 리포트
     */
    public MonthlyAttendanceReportResponse getMonthlyReportByClassroom(Long classroomId, int year, int month) {
        var classroom = classroomService.getClassroom(classroomId);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<com.erp.domain.kid.entity.Kid> kids = kidService.getKidsByClassroom(classroomId);
        List<Attendance> attendances = attendanceRepository.findByClassroomIdAndDateBetween(classroomId, startDate, endDate);
        java.util.Map<Long, List<Attendance>> grouped = attendances.stream()
                .collect(java.util.stream.Collectors.groupingBy(a -> a.getKid().getId()));

        List<MonthlyAttendanceKidReportResponse> kidReports = kids.stream()
                .map(kid -> buildKidReport(kid, grouped.getOrDefault(kid.getId(), List.of())))
                .toList();

        return new MonthlyAttendanceReportResponse(
                classroom.getId(),
                classroom.getName(),
                year,
                month,
                kidReports
        );
    }

    /**
     * 출석 Response 변환
     */
    public AttendanceResponse toResponse(Attendance attendance) {
        return AttendanceResponse.from(attendance);
    }

    private void applyStatus(Attendance attendance,
                             AttendanceStatus status,
                             String note,
                             java.time.LocalTime dropOffTime,
                             java.time.LocalTime pickUpTime) {
        if (status == AttendanceStatus.ABSENT) {
            attendance.markAbsent(note);
            return;
        }
        if (status == AttendanceStatus.SICK_LEAVE) {
            attendance.markSickLeave(note);
            return;
        }
        if (status == AttendanceStatus.LATE) {
            attendance.markLate(dropOffTime, note);
            return;
        }
        if (status == AttendanceStatus.EARLY_LEAVE) {
            attendance.markEarlyLeave(pickUpTime, note);
            return;
        }

        attendance.updateAttendance(status, note);
        if (dropOffTime != null) {
            attendance.recordDropOff(dropOffTime);
        }
        if (pickUpTime != null) {
            attendance.recordPickUp(pickUpTime);
        }
    }

    private MonthlyAttendanceKidReportResponse buildKidReport(com.erp.domain.kid.entity.Kid kid,
                                                              List<Attendance> attendances) {
        int presentDays = 0;
        int absentDays = 0;
        int lateDays = 0;
        int earlyLeaveDays = 0;
        int sickLeaveDays = 0;

        for (Attendance attendance : attendances) {
            AttendanceStatus status = attendance.getStatus();
            if (status == AttendanceStatus.PRESENT) {
                presentDays++;
            } else if (status == AttendanceStatus.ABSENT) {
                absentDays++;
            } else if (status == AttendanceStatus.LATE) {
                lateDays++;
                presentDays++;
            } else if (status == AttendanceStatus.EARLY_LEAVE) {
                earlyLeaveDays++;
                presentDays++;
            } else if (status == AttendanceStatus.SICK_LEAVE) {
                sickLeaveDays++;
                absentDays++;
            }
        }

        return new MonthlyAttendanceKidReportResponse(
                kid.getId(),
                kid.getName(),
                presentDays,
                absentDays,
                lateDays,
                earlyLeaveDays,
                sickLeaveDays,
                attendances.size()
        );
    }

    private void evictDashboardStatisticsByAttendance(Attendance attendance) {
        Long kindergartenId = attendance.getKid().getClassroom().getKindergarten().getId();
        dashboardService.evictDashboardStatisticsCache(kindergartenId);
    }
}
