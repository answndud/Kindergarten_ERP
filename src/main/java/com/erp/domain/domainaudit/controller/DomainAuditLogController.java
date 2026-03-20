package com.erp.domain.domainaudit.controller;

import com.erp.domain.domainaudit.dto.response.DomainAuditLogResponse;
import com.erp.domain.domainaudit.entity.DomainAuditAction;
import com.erp.domain.domainaudit.entity.DomainAuditTargetType;
import com.erp.domain.domainaudit.service.DomainAuditLogQueryService;
import com.erp.global.common.ApiResponse;
import com.erp.global.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/domain-audit-logs")
@RequiredArgsConstructor
public class DomainAuditLogController {

    private final DomainAuditLogQueryService domainAuditLogQueryService;

    @GetMapping
    @PreAuthorize("hasRole('PRINCIPAL')")
    public ResponseEntity<ApiResponse<Page<DomainAuditLogResponse>>> getAuditLogs(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) DomainAuditAction action,
            @RequestParam(required = false) DomainAuditTargetType targetType,
            @RequestParam(required = false) String actorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<DomainAuditLogResponse> responses = domainAuditLogQueryService.getAuditLogsForPrincipal(
                userDetails.getMemberId(),
                action,
                targetType,
                actorName,
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
            @RequestParam(required = false) DomainAuditAction action,
            @RequestParam(required = false) DomainAuditTargetType targetType,
            @RequestParam(required = false) String actorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] csv = domainAuditLogQueryService.exportAuditLogsCsvForPrincipal(
                userDetails.getMemberId(),
                action,
                targetType,
                actorName,
                from,
                to
        );

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("domain-audit-logs-%s.csv".formatted(LocalDate.now()), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(csv);
    }
}
