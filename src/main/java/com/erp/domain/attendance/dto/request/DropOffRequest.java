package com.erp.domain.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * 등원 기록 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class DropOffRequest {

    /**
     * 등원 시간
     */
    @NotNull(message = "등원 시간은 필수입니다")
    private LocalTime dropOffTime;
}
