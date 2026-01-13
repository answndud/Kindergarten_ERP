package com.erp.domain.attendance.dto.request;

import com.erp.domain.attendance.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 출석 등록/수정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class AttendanceRequest {

    /**
     * 원생 ID
     */
    @NotNull(message = "원생 ID는 필수입니다")
    private Long kidId;

    /**
     * 날짜
     */
    @NotNull(message = "날짜는 필수입니다")
    private LocalDate date;

    /**
     * 출석 상태
     */
    @NotNull(message = "출석 상태는 필수입니다")
    private AttendanceStatus status;

    /**
     * 등원 시간
     */
    private LocalTime dropOffTime;

    /**
     * 하원 시간
     */
    private LocalTime pickUpTime;

    /**
     * 메모
     */
    private String note;
}
