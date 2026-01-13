package com.erp.domain.kidapplication.controller;

import com.erp.global.common.ApiResponse;
import com.erp.global.security.user.CustomUserDetails;
import com.erp.domain.kidapplication.dto.request.ApproveKidApplicationRequest;
import com.erp.domain.kidapplication.dto.request.KidApplicationRequest;
import com.erp.domain.kidapplication.dto.request.RejectRequest;
import com.erp.domain.kidapplication.dto.response.KidApplicationResponse;
import com.erp.domain.kidapplication.service.KidApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kid-applications")
@RequiredArgsConstructor
public class KidApplicationController {

    private final KidApplicationService applicationService;

    /**
     * 원생 입학 신청
     */
    @PostMapping
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<Long>> apply(
            @Valid @RequestBody KidApplicationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long applicationId = applicationService.apply(request, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(applicationId));
    }

    /**
     * 내 자녀 입학 신청 목록
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<List<KidApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<KidApplicationResponse> applications = applicationService.getMyApplications(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    /**
     * 유치원별 대기 입학 신청 (교사/원장용)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<KidApplicationResponse>>> getPendingApplications(
            @RequestParam Long kindergartenId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<KidApplicationResponse> applications = applicationService.getPendingApplications(kindergartenId, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    /**
     * 입학 신청 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KidApplicationResponse>> getApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        KidApplicationResponse application = applicationService.getApplication(id, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(application));
    }

    /**
     * 입학 신청 승인
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable Long id,
            @Valid @RequestBody ApproveKidApplicationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        applicationService.approve(id, request, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 입학 신청 거절
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        applicationService.reject(id, request, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 입학 신청 취소
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        applicationService.cancel(id, userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
