package com.erp.domain.notification.service.channel;

import java.util.List;

public interface NotificationChannelSender {

    NotificationChannel channel();

    void send(NotificationDeliveryPayload payload);

    default void sendBatch(List<NotificationDeliveryPayload> payloads) {
        for (NotificationDeliveryPayload payload : payloads) {
            send(payload);
        }
    }
}
