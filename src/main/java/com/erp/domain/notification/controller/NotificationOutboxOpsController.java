package com.erp.domain.notification.controller;

import com.erp.domain.notification.dto.response.NotificationOutboxItemResponse;
import com.erp.domain.notification.dto.response.NotificationOutboxSummaryResponse;
import com.erp.domain.notification.service.NotificationOutboxOpsService;
import com.erp.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification-outbox")
@RequiredArgsConstructor
public class NotificationOutboxOpsController {

    private final NotificationOutboxOpsService notificationOutboxOpsService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<NotificationOutboxSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(notificationOutboxOpsService.getSummary()));
    }

    @GetMapping("/dead-letters")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<Page<NotificationOutboxItemResponse>>> getDeadLetters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationOutboxOpsService.getDeadLetters(page, size)));
    }

    @PostMapping("/{outboxId}/retry")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<NotificationOutboxItemResponse>> retryDeadLetter(@PathVariable Long outboxId) {
        NotificationOutboxItemResponse response = notificationOutboxOpsService.retryDeadLetter(outboxId);
        return ResponseEntity.ok(ApiResponse.success(response, "알림 outbox 재시도가 예약되었습니다"));
    }
}
