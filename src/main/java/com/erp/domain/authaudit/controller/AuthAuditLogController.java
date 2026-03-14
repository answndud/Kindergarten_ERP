package com.erp.domain.authaudit.controller;

import com.erp.domain.authaudit.dto.response.AuthAuditLogResponse;
import com.erp.domain.authaudit.entity.AuthAuditEventType;
import com.erp.domain.authaudit.entity.AuthAuditResult;
import com.erp.domain.authaudit.service.AuthAuditLogQueryService;
import com.erp.domain.member.entity.MemberAuthProvider;
import com.erp.global.common.ApiResponse;
import com.erp.global.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/auth/audit-logs")
@RequiredArgsConstructor
public class AuthAuditLogController {

    private final AuthAuditLogQueryService authAuditLogQueryService;

    @GetMapping
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<Page<AuthAuditLogResponse>>> getAuditLogs(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) AuthAuditEventType eventType,
            @RequestParam(required = false) AuthAuditResult result,
            @RequestParam(required = false) MemberAuthProvider provider,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AuthAuditLogResponse> responses = authAuditLogQueryService.getAuditLogsForPrincipal(
                userDetails.getMemberId(),
                eventType,
                result,
                provider,
                email,
                from,
                to,
                page,
                size
        );

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<byte[]> exportAuditLogs(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) AuthAuditEventType eventType,
            @RequestParam(required = false) AuthAuditResult result,
            @RequestParam(required = false) MemberAuthProvider provider,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] csv = authAuditLogQueryService.exportAuditLogsCsvForPrincipal(
                userDetails.getMemberId(),
                eventType,
                result,
                provider,
                email,
                from,
                to
        );

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("auth-audit-logs-%s.csv".formatted(LocalDate.now()), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(csv);
    }
}
