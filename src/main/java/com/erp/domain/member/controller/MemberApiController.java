package com.erp.domain.member.controller;

import com.erp.domain.member.dto.response.MemberResponse;
import com.erp.domain.member.entity.Member;
import com.erp.domain.member.service.MemberService;
import com.erp.global.common.ApiResponse;
import com.erp.global.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 내 프로필 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Member member = memberService.getMemberById(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(MemberResponse.from(member)));
    }

    /**
     * 프로필 수정
     */
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<MemberResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        memberService.updateProfile(userDetails.getMemberId(), request.getName(), request.getPhone());
        Member updatedMember = memberService.getMemberById(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(MemberResponse.from(updatedMember)));
    }

    /**
     * 비밀번호 변경
     */
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        memberService.changePassword(userDetails.getMemberId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        memberService.withdraw(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ========== Request DTO ==========

    @Data
    public static class UpdateProfileRequest {
        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다")
        private String name;

        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
        private String phone;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        private String newPassword;
    }
}
