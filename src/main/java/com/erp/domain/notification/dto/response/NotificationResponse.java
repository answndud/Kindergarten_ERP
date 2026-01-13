package com.erp.domain.notification.dto.response;

import com.erp.domain.notification.entity.Notification;
import com.erp.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String content,
        String linkUrl,
        boolean isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        String relatedEntityType,
        Long relatedEntityId
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getLinkUrl(),
                notification.getIsRead(),
                notification.getReadAt(),
                notification.getCreatedAt(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId()
        );
    }
}
