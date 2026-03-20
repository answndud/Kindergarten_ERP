package com.erp.domain.attendance.controller;

import com.erp.domain.attendance.dto.request.AttendanceChangeRequestCreateRequest;
import com.erp.domain.attendance.dto.request.AttendanceChangeRequestRejectRequest;
import com.erp.domain.attendance.dto.response.AttendanceChangeRequestResponse;
import com.erp.domain.attendance.service.AttendanceChangeRequestService;
import com.erp.global.common.ApiResponse;
import com.erp.global.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance-requests")
@RequiredArgsConstructor
public class AttendanceChangeRequestController {

    private final AttendanceChangeRequestService attendanceChangeRequestService;

    @PostMapping
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<Long>> create(
            @Valid @RequestBody AttendanceChangeRequestCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = attendanceChangeRequestService.create(request, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<List<AttendanceChangeRequestResponse>>> getMyRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(attendanceChangeRequestService.getMyRequests(userDetails.getMemberId())));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceChangeRequestResponse>>> getPendingRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceChangeRequestService.getPendingRequests(userDetails.getMemberId(), classroomId, date)
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER', 'PARENT')")
    public ResponseEntity<ApiResponse<AttendanceChangeRequestResponse>> getRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(attendanceChangeRequestService.getRequest(id, userDetails.getMemberId())));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        attendanceChangeRequestService.approve(id, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceChangeRequestRejectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        attendanceChangeRequestService.reject(id, request, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        attendanceChangeRequestService.cancel(id, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
