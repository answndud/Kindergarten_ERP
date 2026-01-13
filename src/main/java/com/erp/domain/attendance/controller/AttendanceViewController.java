package com.erp.domain.attendance.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 출결 뷰 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class AttendanceViewController {

    /**
     * 출결관리 페이지
     */
    @GetMapping("/attendance")
    public String attendancePage(@AuthenticationPrincipal Object userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        return "attendance/attendance";
    }
}
