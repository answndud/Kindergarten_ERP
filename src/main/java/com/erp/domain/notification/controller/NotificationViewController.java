package com.erp.domain.notification.controller;

import com.erp.domain.notification.service.NotificationService;
import com.erp.global.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class NotificationViewController {

    private final NotificationService notificationService;

    @GetMapping("/notifications/fragments/badge")
    public String badge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        long count = 0;
        if (userDetails != null) {
            count = notificationService.getUnreadCount(userDetails.getMemberId()).count();
        }

        model.addAttribute("unreadCount", count);
        return "notifications/fragments/badge :: badge";
    }

    @GetMapping("/notifications/fragments/list")
    public String list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            Model model) {

        if (userDetails == null) {
            model.addAttribute("notifications", java.util.List.of());
            model.addAttribute("unreadOnly", unreadOnly);
            return "notifications/fragments/list :: list";
        }

        if (unreadOnly) {
            model.addAttribute("notifications", notificationService.getUnreadNotifications(userDetails.getMemberId()));
        } else {
            model.addAttribute("notifications", notificationService.getNotifications(userDetails.getMemberId(), limit));
        }

        model.addAttribute("unreadOnly", unreadOnly);
        return "notifications/fragments/list :: list";
    }
}
