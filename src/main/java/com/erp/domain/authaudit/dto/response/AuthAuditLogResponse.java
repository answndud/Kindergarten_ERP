package com.erp.domain.authaudit.dto.response;

import com.erp.domain.authaudit.entity.AuthAuditEventType;
import com.erp.domain.authaudit.entity.AuthAuditLog;
import com.erp.domain.authaudit.entity.AuthAuditResult;
import com.erp.domain.member.entity.MemberAuthProvider;

import java.time.LocalDateTime;

public record AuthAuditLogResponse(
        Long id,
        Long memberId,
        String email,
        MemberAuthProvider provider,
        AuthAuditEventType eventType,
        AuthAuditResult result,
        String reason,
        String clientIp,
        LocalDateTime createdAt
) {

    public static AuthAuditLogResponse from(AuthAuditLog authAuditLog) {
        return new AuthAuditLogResponse(
                authAuditLog.getId(),
                authAuditLog.getMemberId(),
                authAuditLog.getEmail(),
                authAuditLog.getProvider(),
                authAuditLog.getEventType(),
                authAuditLog.getResult(),
                authAuditLog.getReason(),
                authAuditLog.getClientIp(),
                authAuditLog.getCreatedAt()
        );
    }
}
