package com.erp.domain.attendance.dto.response;

import com.erp.domain.attendance.entity.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 일별 출석 간단 응답 DTO
 */
public record DailyAttendanceResponse(
        Long kidId,
        String kidName,
        AttendanceStatus status,
        LocalTime dropOffTime,
        LocalTime pickUpTime
) {
    /**
     * Attendance 엔티티를 간단 DTO로 변환
     */
    public static DailyAttendanceResponse from(com.erp.domain.attendance.entity.Attendance attendance) {
        return new DailyAttendanceResponse(
                attendance.getKid().getId(),
                attendance.getKid().getName(),
                attendance.getStatus(),
                attendance.getDropOffTime(),
                attendance.getPickUpTime()
        );
    }
}
