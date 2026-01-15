package com.erp.domain.kid.controller;

import com.erp.global.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class KidViewController {

    @GetMapping("/kids")
    @PreAuthorize("hasAnyRole('PRINCIPAL', 'TEACHER')")
    public String kidsPage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return "kid/kids";
    }
}
