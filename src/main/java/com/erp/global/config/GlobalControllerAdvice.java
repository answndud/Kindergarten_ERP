package com.erp.global.config;

import com.erp.domain.member.dto.response.MemberResponse;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.service.MemberService;
import com.erp.global.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 전역 컨트롤러 어드바이스
 * 모든 컨트롤러에 공통으로 적용되는 설정
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final MemberService memberService;

    /**
     * 현재 로그인한 회원 정보를 모든 뷰에 전달
     */
    @ModelAttribute("currentMember")
    public MemberResponse currentMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.debug("currentMember: userDetails is null");
            return null;
        }

        try {
            log.debug("currentMember: loading member with id={}", userDetails.getMemberId());
            // LazyInitializationException 방지를 위해 유치원 포함하여 조회
            Member member = memberService.getMemberByIdWithKindergarten(userDetails.getMemberId());
            MemberResponse response = MemberResponse.from(member);
            log.debug("currentMember: loaded successfully - {}", response.name());
            return response;
        } catch (Exception e) {
            log.error("currentMember: failed to load member", e);
            return null;
        }
    }
}
