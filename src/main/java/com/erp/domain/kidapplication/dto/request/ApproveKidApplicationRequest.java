package com.erp.domain.kidapplication.dto.request;

import jakarta.validation.constraints.NotNull;

public record ApproveKidApplicationRequest(
        @NotNull(message = "반 배정은 필수입니다")
        Long classroomId
) {
}
