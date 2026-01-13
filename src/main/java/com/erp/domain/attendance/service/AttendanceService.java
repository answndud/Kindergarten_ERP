package com.erp.domain.attendance.service;

import com.erp.domain.attendance.dto.request.AttendanceRequest;
import com.erp.domain.attendance.dto.request.DropOffRequest;
import com.erp.domain.attendance.dto.request.PickUpRequest;
import com.erp.domain.attendance.dto.response.AttendanceResponse;
import com.erp.domain.attendance.dto.response.DailyAttendanceResponse;
import com.erp.domain.attendance.dto.response.MonthlyStatisticsResponse;
import com.erp.domain.attendance.entity.Attendance;
import com.erp.domain.attendance.entity.AttendanceStatus;
import com.erp.domain.attendance.repository.AttendanceRepository;
import com.erp.domain.classroom.service.ClassroomService;
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

    /**
     * 출석 등록
     */
    @Transactional
    public Long createAttendance(AttendanceRequest request) {
        // 원생 조회
        kidService.getKid(request.getKidId());

        // 중복 확인
        if (attendanceRepository.findByKidIdAndDate(request.getKidId(), request.getDate()).isPresent()) {
            throw new BusinessException(ErrorCode.ATTENDANCE_ALREADY_EXISTS);
        }

        // 출석 생성
        Attendance attendance = Attendance.create(
                kidService.getKid(request.getKidId()),
                request.getDate(),
                request.getStatus()
        );

        // 시간과 메모 설정
        if (request.getDropOffTime() != null) {
            attendance.recordDropOff(request.getDropOffTime());
        }
        if (request.getPickUpTime() != null) {
            attendance.recordPickUp(request.getPickUpTime());
        }
        if (request.getNote() != null) {
            attendance.updateAttendance(request.getStatus(), request.getNote());
        }

        Attendance saved = attendanceRepository.save(attendance);
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

        attendance.updateAttendance(request.getStatus(), request.getNote());

        if (request.getDropOffTime() != null) {
            attendance.recordDropOff(request.getDropOffTime());
        }
        if (request.getPickUpTime() != null) {
            attendance.recordPickUp(request.getPickUpTime());
        }
    }

    /**
     * 등원 기록
     */
    @Transactional
    public void recordDropOff(Long kidId, LocalDate date, DropOffRequest request) {
        // 원생 존재 확인
        kidService.getKid(kidId);

        // 기존 출석 확인
        Attendance attendance = attendanceRepository.findByKidIdAndDate(kidId, date)
                .orElseGet(() -> {
                    // 없으면 새로 생성
                    Attendance newAttendance = Attendance.createDropOff(
                            kidService.getKid(kidId),
                            date,
                            request.getDropOffTime()
                    );
                    return attendanceRepository.save(newAttendance);
                });

        attendance.recordDropOff(request.getDropOffTime());
    }

    /**
     * 하원 기록
     */
    @Transactional
    public void recordPickUp(Long kidId, LocalDate date, PickUpRequest request) {
        // 원생 존재 확인
        kidService.getKid(kidId);

        Attendance attendance = attendanceRepository.findByKidIdAndDate(kidId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));

        attendance.recordPickUp(request.getPickUpTime());
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
    }

    /**
     * 조퇴 처리
     */
    @Transactional
    public void markEarlyLeave(Long kidId, LocalDate date, java.time.LocalTime pickUpTime, String note) {
        Attendance attendance = attendanceRepository.findByKidIdAndDate(kidId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));

        attendance.markEarlyLeave(pickUpTime, note);
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
    }

    /**
     * 출석 삭제
     */
    @Transactional
    public void deleteAttendance(Long id) {
        Attendance attendance = getAttendance(id);
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
        List<Attendance> attendances = getAttendancesByClassroomAndDate(classroomId, date);

        return attendances.stream()
                .map(DailyAttendanceResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 출석 Response 변환
     */
    public AttendanceResponse toResponse(Attendance attendance) {
        return AttendanceResponse.from(attendance);
    }
}
