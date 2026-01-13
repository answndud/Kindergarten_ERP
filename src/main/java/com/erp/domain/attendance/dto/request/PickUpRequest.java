package com.erp.domain.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 하원 기록 요청 DTO
 */
@Getter
@NoArgsConstructor
public class PickUpRequest {

    /**
     * 하원 시간
     */
    @NotNull(message = "하원 시간은 필수입니다")
    private LocalTime pickUpTime;
}
