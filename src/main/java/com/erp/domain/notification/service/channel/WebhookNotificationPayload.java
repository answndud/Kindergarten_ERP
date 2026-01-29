package com.erp.domain.notification.service.channel;

import com.erp.domain.notification.entity.NotificationType;

public record WebhookNotificationPayload(
        Long receiverId,
        String receiverEmail,
        String receiverName,
        NotificationType type,
        String title,
        String content,
        String linkUrl
) {
    public static WebhookNotificationPayload from(NotificationDeliveryPayload payload) {
        return new WebhookNotificationPayload(
                payload.receiverId(),
                payload.receiverEmail(),
                payload.receiverName(),
                payload.type(),
                payload.title(),
                payload.content(),
                payload.linkUrl()
        );
    }
}
