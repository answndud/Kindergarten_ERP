package com.erp.domain.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "notification.delivery")
public class NotificationDeliveryProperties {

    private int batchSize = 500;
    private Email email = new Email();
    private Webhook push = new Webhook();
    private Webhook app = new Webhook();

    @Getter
    @Setter
    public static class Email {
        private boolean enabled = false;
        private String from;
        private String subjectPrefix = "[KinderCare]";
    }

    @Getter
    @Setter
    public static class Webhook {
        private boolean enabled = false;
        private String webhookUrl;
    }
}
