package com.erp.domain.dashboard.controller;

import com.erp.domain.dashboard.dto.response.DashboardStatisticsResponse;
import com.erp.domain.dashboard.service.DashboardService;
import com.erp.domain.kindergarten.entity.Kindergarten;
import com.erp.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<DashboardStatisticsResponse>> getStatistics(
            @AuthenticationPrincipal com.erp.global.security.user.CustomUserDetails userDetails) {
        Kindergarten kindergarten = userDetails.getMember().getKindergarten();
        DashboardStatisticsResponse response = dashboardService.getDashboardStatistics(kindergarten);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
