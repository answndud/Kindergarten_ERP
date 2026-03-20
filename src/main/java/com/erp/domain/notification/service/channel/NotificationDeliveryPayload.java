package com.erp.domain.notification.service.channel;

import com.erp.domain.notification.entity.NotificationType;

public record NotificationDeliveryPayload(
        Long notificationId,
        Long receiverId,
        String receiverEmail,
        String receiverName,
        NotificationType type,
        String title,
        String content,
        String linkUrl
) {
}
