package com.erp.domain.attendance.controller;

import com.erp.domain.attendance.dto.request.AttendanceRequest;
import com.erp.domain.attendance.dto.request.BulkAttendanceRequest;
import com.erp.domain.attendance.dto.request.DropOffRequest;
import com.erp.domain.attendance.dto.request.PickUpRequest;
import com.erp.domain.attendance.dto.response.AttendanceResponse;
import com.erp.domain.attendance.dto.response.BulkAttendanceResponse;
import com.erp.domain.attendance.dto.response.DailyAttendanceResponse;
import com.erp.domain.attendance.dto.response.MonthlyAttendanceReportResponse;
import com.erp.domain.attendance.dto.response.MonthlyStatisticsResponse;
import com.erp.domain.attendance.entity.Attendance;
import com.erp.domain.attendance.service.AttendanceService;
import com.erp.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 출석 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 출석 등록 (교사만 가능)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> create(
            @Valid @RequestBody AttendanceRequest request) {

        Long id = attendanceService.createAttendance(request);

        Attendance attendance = attendanceService.getAttendance(id);

        return ResponseEntity
                .ok(ApiResponse.success(attendanceService.toResponse(attendance), "출석이 등록되었습니다"));
    }

    /**
     * 출석 등록/수정 (Upsert)
     */
    @PostMapping("/upsert")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> upsert(
            @Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse response = attendanceService.upsertAttendance(request);
        return ResponseEntity.ok(ApiResponse.success(response, "출석이 저장되었습니다"));
    }

    /**
     * 반별 일괄 출석 처리
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<BulkAttendanceResponse>> bulkUpdate(
            @Valid @RequestBody BulkAttendanceRequest request) {
        int updated = attendanceService.bulkUpdateAttendance(request);
        return ResponseEntity.ok(ApiResponse.success(new BulkAttendanceResponse(updated), "일괄 출석 처리가 완료되었습니다"));
    }

    /**
     * 출석 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendance(@PathVariable Long id) {
        Attendance attendance = attendanceService.getAttendance(id);

        return ResponseEntity
                .ok(ApiResponse.success(attendanceService.toResponse(attendance)));
    }

    /**
     * 원생별 날짜 출석 조회
     */
    @GetMapping("/kid/{kidId}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceByKidAndDate(
            @PathVariable Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Attendance attendance = attendanceService.getAttendanceByKidAndDate(kidId, date);

        return ResponseEntity
                .ok(ApiResponse.success(attendanceService.toResponse(attendance)));
    }

    /**
     * 반별 일별 출석 현황
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<DailyAttendanceResponse>>> getDailyAttendance(
            @RequestParam(required = false) Long classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (classroomId == null) {
            return ResponseEntity
                    .ok(ApiResponse.success(List.of(), "반 ID는 필수입니다"));
        }

        List<DailyAttendanceResponse> responses = attendanceService.getDailyAttendanceByClassroom(classroomId, date);

        return ResponseEntity
                .ok(ApiResponse.success(responses));
    }

    /**
     * 원생별 월간 출석 목록
     */
    @GetMapping("/kid/{kidId}/monthly")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getMonthlyAttendances(
            @PathVariable Long kidId,
            @RequestParam int year,
            @RequestParam int month) {

        List<Attendance> attendances = attendanceService.getAttendancesByKidAndMonth(kidId, year, month);

        List<AttendanceResponse> responses = attendances.stream()
                .map(attendanceService::toResponse)
                .toList();

        return ResponseEntity
                .ok(ApiResponse.success(responses));
    }

    /**
     * 반별 월간 리포트
     */
    @GetMapping("/classroom/{classroomId}/monthly-report")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<MonthlyAttendanceReportResponse>> getMonthlyReport(
            @PathVariable Long classroomId,
            @RequestParam int year,
            @RequestParam int month) {
        MonthlyAttendanceReportResponse response = attendanceService.getMonthlyReportByClassroom(classroomId, year, month);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 월간 출석 통계
     */
    @GetMapping("/kid/{kidId}/statistics")
    public ResponseEntity<ApiResponse<MonthlyStatisticsResponse>> getMonthlyStatistics(
            @PathVariable Long kidId,
            @RequestParam int year,
            @RequestParam int month) {

        MonthlyStatisticsResponse response = attendanceService.getMonthlyStatistics(kidId, year, month);

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    /**
     * 출석 수정 (교사만 가능)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequest request) {

        attendanceService.updateAttendance(id, request);

        Attendance attendance = attendanceService.getAttendance(id);

        return ResponseEntity
                .ok(ApiResponse.success(attendanceService.toResponse(attendance), "출석 정보가 수정되었습니다"));
    }

    /**
     * 등원 기록 (교사만 가능)
     */
    @PostMapping("/kid/{kidId}/drop-off")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> recordDropOff(
            @PathVariable Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody DropOffRequest request) {

        attendanceService.recordDropOff(kidId, date, request);

        Attendance attendance = attendanceService.getAttendanceByKidAndDate(kidId, date);

        return ResponseEntity
                .ok(ApiResponse.success(attendanceService.toResponse(attendance), "등원이 기록되었습니다"));
    }

    /**
     * 하원 기록 (교사만 가능)
     */
    @PostMapping("/kid/{kidId}/pick-up")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> recordPickUp(
            @PathVariable Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody PickUpRequest request) {

        attendanceService.recordPickUp(kidId, date, request);

        Attendance attendance = attendanceService.getAttendanceByKidAndDate(kidId, date);

        return ResponseEntity
                .ok(ApiResponse.success(attendanceService.toResponse(attendance), "하원이 기록되었습니다"));
    }

    /**
     * 결석 처리 (교사만 가능)
     */
    @PostMapping("/kid/{kidId}/absent")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAbsent(
            @PathVariable Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String note) {

        attendanceService.markAbsent(kidId, date, note);

        Attendance attendance = attendanceService.getAttendanceByKidAndDate(kidId, date);

        return ResponseEntity
                .ok(ApiResponse.success(attendanceService.toResponse(attendance), "결석 처리되었습니다"));
    }

    /**
     * 지각 처리 (교사만 가능)
     */
    @PostMapping("/kid/{kidId}/late")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markLate(
            @PathVariable Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) java.time.LocalTime dropOffTime,
            @RequestParam(required = false) String note) {

        attendanceService.markLate(kidId, date, dropOffTime, note);

        Attendance attendance = attendanceService.getAttendanceByKidAndDate(kidId, date);

        return ResponseEntity
                .ok(ApiResponse.success(attendanceService.toResponse(attendance), "지각 처리되었습니다"));
    }

    /**
     * 조퇴 처리 (교사만 가능)
     */
    @PostMapping("/kid/{kidId}/early-leave")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markEarlyLeave(
            @PathVariable Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) java.time.LocalTime pickUpTime,
            @RequestParam(required = false) String note) {

        attendanceService.markEarlyLeave(kidId, date, pickUpTime, note);

        Attendance attendance = attendanceService.getAttendanceByKidAndDate(kidId, date);

        return ResponseEntity
                .ok(ApiResponse.success(attendanceService.toResponse(attendance), "조퇴 처리되었습니다"));
    }

    /**
     * 병결 처리 (교사만 가능)
     */
    @PostMapping("/kid/{kidId}/sick-leave")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markSickLeave(
            @PathVariable Long kidId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String note) {

        attendanceService.markSickLeave(kidId, date, note);

        Attendance attendance = attendanceService.getAttendanceByKidAndDate(kidId, date);

        return ResponseEntity
                .ok(ApiResponse.success(attendanceService.toResponse(attendance), "병결 처리되었습니다"));
    }

    /**
     * 출석 삭제 (교사만 가능)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);

        return ResponseEntity
                .ok(ApiResponse.success(null, "출석 정보가 삭제되었습니다"));
    }
}
