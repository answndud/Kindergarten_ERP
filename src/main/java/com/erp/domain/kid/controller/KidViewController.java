package com.erp.domain.kid.controller;

import com.erp.domain.kid.dto.response.KidDetailResponse;
import com.erp.domain.kid.service.KidService;
import com.erp.global.security.user.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class KidViewController {

    private final KidService kidService;

    public KidViewController(KidService kidService) {
        this.kidService = kidService;
    }

    @GetMapping("/kids")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String kidsPage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return "kid/kids";
    }

    @GetMapping("/kids/{id}")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String kidDetailPage(@PathVariable Long id, Model model) {
        KidDetailResponse detail = kidService.getKidDetail(id);
        model.addAttribute("kid", detail);
        return "kid/kid-detail";
    }

    @GetMapping("/kids/new")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String kidCreatePage() {
        return "kid/kid-form";
    }

    @GetMapping("/kids/{id}/edit")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String kidEditPage(@PathVariable Long id, Model model) {
        KidDetailResponse detail = kidService.getKidDetail(id);
        model.addAttribute("kid", detail);
        return "kid/kid-form";
    }
}
