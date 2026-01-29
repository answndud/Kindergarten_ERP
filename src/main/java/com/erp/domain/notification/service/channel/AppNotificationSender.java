package com.erp.domain.notification.service.channel;

import com.erp.domain.notification.config.NotificationDeliveryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.delivery.app", name = "enabled", havingValue = "true")
public class AppNotificationSender implements NotificationChannelSender {

    private final NotificationDeliveryProperties deliveryProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.APP;
    }

    @Override
    public void send(NotificationDeliveryPayload payload) {
        String webhookUrl = deliveryProperties.getApp().getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        WebhookNotificationPayload body = WebhookNotificationPayload.from(payload);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    webhookUrl,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            log.debug("App notification dispatched. receiverId={}, status={}", payload.receiverId(), response.getStatusCode());
        } catch (Exception e) {
            log.warn("App notification failed. receiverId={}, error={}", payload.receiverId(), e.getMessage());
        }
    }
}
