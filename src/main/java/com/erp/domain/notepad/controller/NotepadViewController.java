package com.erp.domain.notepad.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 알림장 뷰 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class NotepadViewController {

    /**
     * 알림장 페이지
     */
    @GetMapping("/notepad")
    public String notepadPage(@AuthenticationPrincipal Object userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        return "notepad/notepad";
    }
}
