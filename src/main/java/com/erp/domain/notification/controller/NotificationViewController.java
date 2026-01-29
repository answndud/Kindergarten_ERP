package com.erp.domain.notification.controller;

import com.erp.domain.notification.service.NotificationService;
import com.erp.domain.notification.entity.NotificationType;
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

    @GetMapping("/notifications")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public String notificationsPage(Model model) {
        model.addAttribute("notificationTypes", NotificationType.values());
        return "notifications/index";
    }

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
            @RequestParam(required = false) NotificationType type,
            @RequestParam(defaultValue = "false") boolean showFilters,
            Model model) {

        if (userDetails == null) {
            model.addAttribute("notifications", java.util.List.of());
            model.addAttribute("unreadOnly", unreadOnly);
            model.addAttribute("unreadCount", 0);
            model.addAttribute("type", type);
            model.addAttribute("showFilters", showFilters);
            model.addAttribute("notificationTypes", NotificationType.values());
            return "notifications/fragments/list :: list";
        }

        if (unreadOnly && type != null) {
            model.addAttribute("notifications", notificationService.getUnreadNotificationsByType(userDetails.getMemberId(), type, limit));
        } else if (unreadOnly) {
            model.addAttribute("notifications", notificationService.getUnreadNotifications(userDetails.getMemberId(), limit));
        } else if (type != null) {
            model.addAttribute("notifications", notificationService.getNotificationsByType(userDetails.getMemberId(), type, limit));
        } else {
            model.addAttribute("notifications", notificationService.getNotifications(userDetails.getMemberId(), limit));
        }

        model.addAttribute("unreadOnly", unreadOnly);
        model.addAttribute("type", type);
        model.addAttribute("showFilters", showFilters);
        model.addAttribute("notificationTypes", NotificationType.values());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(userDetails.getMemberId()).count());
        return "notifications/fragments/list :: list";
    }
}
