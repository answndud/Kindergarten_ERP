package com.erp.domain.notepad.controller;

import com.erp.domain.classroom.service.ClassroomService;
import com.erp.domain.kid.service.KidService;
import com.erp.domain.notepad.dto.request.NotepadRequest;
import com.erp.domain.notepad.dto.response.NotepadDetailResponse;
import com.erp.domain.notepad.dto.response.NotepadResponse;
import com.erp.domain.notepad.service.NotepadService;
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
 * 알림장 뷰 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class NotepadViewController {

    private final NotepadService notepadService;
    private final ClassroomService classroomService;
    private final KidService kidService;

    /**
     * 알림장 목록 페이지
     */
    @GetMapping("/notepad")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER', 'PARENT')")
    public String notepadPage(@AuthenticationPrincipal Object userDetails) {
        return "notepad/notepad";
    }

    /**
     * 알림장 작성 페이지
     */
    @GetMapping("/notepad/write")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String writeForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        // TODO: Load classrooms and kids for the current user's kindergarten
        // For now, the template will fetch these via API calls
        return "notepad/write";
    }

    /**
     * 알림장 상세 페이지
     */
    @GetMapping("/notepad/{id}")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER', 'PARENT')")
    public String detail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        NotepadDetailResponse notepad = notepadService.getNotepadDetail(id, userDetails.getMemberId());

        // 읽음 표시
        try {
            notepadService.markAsRead(id, userDetails.getMemberId());
        } catch (Exception ignored) {
            // 이미 읽었거나 권한 없는 경우 무시
        }

        model.addAttribute("notepad", notepad);
        return "notepad/detail";
    }

    /**
     * 알림장 수정 페이지
     */
    @GetMapping("/notepad/{id}/edit")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String editForm(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        NotepadDetailResponse notepad = notepadService.getNotepadDetail(id, userDetails.getMemberId());
        model.addAttribute("notepad", notepad);
        return "notepad/edit";
    }

    /**
     * 알림장 작성
     */
    @PostMapping("/notepad")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String write(
            @ModelAttribute NotepadRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Long notepadId = notepadService.createNotepad(request, userDetails.getMemberId());
            redirectAttributes.addFlashAttribute("message", "알림장이 작성되었습니다.");
            return "redirect:/notepad/" + notepadId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "알림장 작성에 실패했습니다: " + e.getMessage());
            return "redirect:/notepad/write";
        }
    }

    /**
     * 알림장 수정
     */
    @PostMapping("/notepad/{id}")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String update(
            @PathVariable Long id,
            @ModelAttribute NotepadRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            notepadService.updateNotepad(id, request, userDetails.getMemberId());
            redirectAttributes.addFlashAttribute("message", "알림장이 수정되었습니다.");
            return "redirect:/notepad/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "알림장 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/notepad/" + id + "/edit";
        }
    }

    /**
     * 알림장 삭제
     */
    @PostMapping("/notepad/{id}/delete")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            notepadService.deleteNotepad(id, userDetails.getMemberId());
            redirectAttributes.addFlashAttribute("message", "알림장이 삭제되었습니다.");
            return "redirect:/notepad";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "알림장 삭제에 실패했습니다: " + e.getMessage());
            return "redirect:/notepad/" + id;
        }
    }
}
