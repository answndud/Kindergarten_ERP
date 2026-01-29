package com.erp.domain.notification.service;

import com.erp.domain.notification.entity.Notification;
import com.erp.domain.notification.service.channel.NotificationChannelSender;
import com.erp.domain.notification.service.channel.NotificationDeliveryPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final List<NotificationChannelSender> channelSenders;

    public void dispatch(Notification notification) {
        if (notification == null || channelSenders == null || channelSenders.isEmpty()) {
            return;
        }

        NotificationDeliveryPayload payload = toPayload(notification);
        for (NotificationChannelSender sender : channelSenders) {
            try {
                sender.send(payload);
            } catch (Exception ex) {
                log.warn("Notification dispatch failed. channel={}, receiverId={}, error={}",
                        sender.channel(), payload.receiverId(), ex.getMessage());
            }
        }
    }

    public void dispatch(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty() || channelSenders == null || channelSenders.isEmpty()) {
            return;
        }

        for (Notification notification : notifications) {
            dispatch(notification);
        }
    }

    private NotificationDeliveryPayload toPayload(Notification notification) {
        var receiver = notification.getReceiver();
        return new NotificationDeliveryPayload(
                receiver != null ? receiver.getId() : null,
                receiver != null ? receiver.getEmail() : null,
                receiver != null ? receiver.getName() : null,
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getLinkUrl()
        );
    }
}
