package com.erp.domain.attendance.dto.response;

import java.util.List;

public record MonthlyAttendanceReportResponse(
        Long classroomId,
        String classroomName,
        int year,
        int month,
        List<MonthlyAttendanceKidReportResponse> kids
) {
}
