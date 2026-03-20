package com.erp.domain.attendance.entity;

public enum AttendanceChangeRequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED;

    public boolean isPending() {
        return this == PENDING;
    }
}
