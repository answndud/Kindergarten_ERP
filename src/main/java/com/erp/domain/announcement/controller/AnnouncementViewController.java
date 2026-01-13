package com.erp.domain.announcement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 공지사항 뷰 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class AnnouncementViewController {

    /**
     * 공지사항 페이지
     */
    @GetMapping("/announcements")
    public String announcementsPage(@AuthenticationPrincipal Object userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        return "announcement/announcements";
    }
}
