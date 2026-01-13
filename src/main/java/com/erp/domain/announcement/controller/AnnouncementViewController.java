package com.erp.domain.announcement.controller;

import com.erp.domain.announcement.dto.request.AnnouncementRequest;
import com.erp.domain.announcement.entity.Announcement;
import com.erp.domain.announcement.service.AnnouncementService;
import com.erp.global.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 공지사항 뷰 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class AnnouncementViewController {

    private final AnnouncementService announcementService;

    /**
     * 공지사항 목록 페이지
     */
    @GetMapping("/announcements")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER', 'PARENT')")
    public String announcementsPage(@AuthenticationPrincipal Object userDetails) {
        return "announcement/announcements";
    }

    /**
     * 공지사항 작성 페이지
     */
    @GetMapping("/announcement/write")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String writeForm(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return "announcement/write";
    }

    /**
     * 공지사항 상세 페이지
     */
    @GetMapping("/announcement/{id}")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER', 'PARENT')")
    public String detail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        // 조회수 증가 없이 조회
        Announcement announcement = announcementService.getAnnouncementWithoutIncrement(id);
        model.addAttribute("announcement", announcementService.toResponse(announcement));
        return "announcement/detail";
    }

    /**
     * 공지사항 수정 페이지
     */
    @GetMapping("/announcement/{id}/edit")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String editForm(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Announcement announcement = announcementService.getAnnouncementWithoutIncrement(id);
        model.addAttribute("announcement", announcementService.toResponse(announcement));
        return "announcement/edit";
    }

    /**
     * 공지사항 작성
     */
    @PostMapping("/announcement")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String write(
            @ModelAttribute AnnouncementRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Long announcementId = announcementService.createAnnouncement(request, userDetails.getMemberId());
            redirectAttributes.addFlashAttribute("message", "공지사항이 작성되었습니다.");
            return "redirect:/announcement/" + announcementId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "공지사항 작성에 실패했습니다: " + e.getMessage());
            return "redirect:/announcement/write";
        }
    }

    /**
     * 공지사항 수정
     */
    @PostMapping("/announcement/{id}")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String update(
            @PathVariable Long id,
            @ModelAttribute AnnouncementRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            announcementService.updateAnnouncement(id, request, userDetails.getMemberId());
            redirectAttributes.addFlashAttribute("message", "공지사항이 수정되었습니다.");
            return "redirect:/announcement/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "공지사항 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/announcement/" + id + "/edit";
        }
    }

    /**
     * 공지사항 삭제
     */
    @PostMapping("/announcement/{id}/delete")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            announcementService.deleteAnnouncement(id, userDetails.getMemberId());
            redirectAttributes.addFlashAttribute("message", "공지사항이 삭제되었습니다.");
            return "redirect:/announcements";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "공지사항 삭제에 실패했습니다: " + e.getMessage());
            return "redirect:/announcement/" + id;
        }
    }

    /**
     * 중요 공지 토글
     */
    @PostMapping("/announcement/{id}/toggle-important")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String toggleImportant(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            announcementService.toggleImportant(id, userDetails.getMemberId());
            redirectAttributes.addFlashAttribute("message", "중요 공지 설정이 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "중요 공지 설정 변경에 실패했습니다: " + e.getMessage());
        }
        return "redirect:/announcement/" + id;
    }
}
