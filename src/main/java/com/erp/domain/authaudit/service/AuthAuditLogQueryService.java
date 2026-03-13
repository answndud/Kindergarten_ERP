package com.erp.domain.authaudit.service;

import com.erp.domain.authaudit.dto.response.AuthAuditLogResponse;
import com.erp.domain.authaudit.entity.AuthAuditEventType;
import com.erp.domain.authaudit.entity.AuthAuditResult;
import com.erp.domain.authaudit.repository.AuthAuditLogRepository;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.entity.MemberAuthProvider;
import com.erp.domain.member.entity.MemberRole;
import com.erp.domain.member.service.MemberService;
import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthAuditLogQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AuthAuditLogRepository authAuditLogRepository;
    private final MemberService memberService;

    public Page<AuthAuditLogResponse> getAuditLogsForPrincipal(Long requesterId,
                                                               AuthAuditEventType eventType,
                                                               AuthAuditResult result,
                                                               MemberAuthProvider provider,
                                                               String email,
                                                               LocalDate from,
                                                               LocalDate to,
                                                               int page,
                                                               int size) {
        Member requester = memberService.getMemberByIdWithKindergarten(requesterId);
        validateRequester(requester);

        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );

        return authAuditLogRepository.searchByKindergartenId(
                requester.getKindergarten().getId(),
                eventType,
                result,
                provider,
                normalizeEmailKeyword(email),
                atStartOfDay(from),
                toExclusive(to),
                pageRequest
        ).map(AuthAuditLogResponse::from);
    }

    private void validateRequester(Member requester) {
        if (requester.getRole() != MemberRole.PRINCIPAL || requester.getKindergarten() == null) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String normalizeEmailKeyword(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private LocalDateTime atStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    private LocalDateTime toExclusive(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.plusDays(1).atStartOfDay();
    }
}
