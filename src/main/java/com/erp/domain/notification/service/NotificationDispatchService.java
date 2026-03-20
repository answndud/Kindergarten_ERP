package com.erp.domain.notification.service;

import com.erp.domain.notification.config.NotificationDeliveryProperties;
import com.erp.domain.notification.entity.NotificationDeliveryStatus;
import com.erp.domain.notification.entity.Notification;
import com.erp.domain.notification.entity.NotificationOutbox;
import com.erp.domain.notification.repository.NotificationOutboxRepository;
import com.erp.domain.notification.service.channel.NotificationChannel;
import com.erp.domain.notification.service.channel.NotificationChannelSender;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationDispatchService {

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotificationDeliveryPolicyService notificationDeliveryPolicyService;
    private final NotificationDeliveryProperties deliveryProperties;
    private final List<NotificationChannelSender> channelSenders;
    private final PlatformTransactionManager transactionManager;

    @Transactional
    public void dispatch(Notification notification) {
        if (notification == null) {
            return;
        }

        enqueueNotifications(List.of(notification));
    }

    @Transactional
    public void dispatch(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }

        enqueueNotifications(notifications);
    }

    @Scheduled(fixedDelayString = "${notification.delivery.worker-fixed-delay-ms:15000}")
    public void processReadyDeliveriesOnSchedule() {
        if (!deliveryProperties.isWorkerEnabled()) {
            return;
        }
        processReadyDeliveriesBatch();
    }

    public int processReadyDeliveriesBatch() {
        LocalDateTime now = LocalDateTime.now();
        List<Long> claimedIds = executeInNewTransaction(() -> claimReadyDeliveries(now));
        claimedIds.forEach(outboxId -> executeInNewTransaction(() -> {
            processClaimedDelivery(outboxId);
            return null;
        }));
        return claimedIds.size();
    }

    private List<Long> claimReadyDeliveries(LocalDateTime now) {
        int batchSize = resolveWorkerBatchSize();
        LocalDateTime staleBefore = now.minus(resolveProcessingTimeout());

        List<NotificationOutbox> readyBatch = notificationOutboxRepository
                .findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAscIdAsc(
                        NotificationDeliveryStatus.PENDING,
                        now,
                        PageRequest.of(0, batchSize)
                );

        if (readyBatch.isEmpty()) {
            readyBatch = notificationOutboxRepository
                    .findByStatusAndProcessingStartedAtLessThanEqualOrderByProcessingStartedAtAscIdAsc(
                            NotificationDeliveryStatus.PROCESSING,
                            staleBefore,
                            PageRequest.of(0, batchSize)
                    );
        }

        readyBatch.forEach(outbox -> outbox.markProcessing(now));
        notificationOutboxRepository.flush();
        return readyBatch.stream()
                .map(NotificationOutbox::getId)
                .toList();
    }

    private void processClaimedDelivery(Long outboxId) {
        NotificationOutbox outbox = notificationOutboxRepository.findById(outboxId)
                .orElse(null);
        if (outbox == null || outbox.getChannel() == null) {
            return;
        }

        NotificationChannelSender sender = resolveSender(outbox.getChannel());
        LocalDateTime now = LocalDateTime.now();

        try {
            sender.send(outbox.toPayload());
            outbox.markDelivered(now);
            log.debug("Notification outbox delivered. outboxId={}, channel={}, notificationId={}",
                    outbox.getId(), outbox.getChannel(), outbox.getNotification().getId());
        } catch (Exception ex) {
            if (!outbox.canRetry()) {
                outbox.markDeadLetter(now, ex.getMessage());
                log.warn("Notification outbox dead-lettered. outboxId={}, channel={}, attempts={}, error={}",
                        outbox.getId(), outbox.getChannel(), outbox.getAttemptCount(), ex.getMessage());
                return;
            }

            Duration retryDelay = resolveRetryDelay(outbox.getAttemptCount());
            outbox.scheduleRetry(now.plus(retryDelay), ex.getMessage());
            log.warn("Notification outbox rescheduled. outboxId={}, channel={}, attempts={}, retryAfterMs={}, error={}",
                    outbox.getId(), outbox.getChannel(), outbox.getAttemptCount(), retryDelay.toMillis(), ex.getMessage());
        }
    }

    private void enqueueNotifications(List<Notification> notifications) {
        List<NotificationOutbox> outboxes = notifications.stream()
                .filter(notification -> notification != null && notification.getId() != null)
                .flatMap(notification -> notificationDeliveryPolicyService.resolveChannels(notification).stream()
                        .map(channel -> NotificationOutbox.create(notification, channel, resolveMaxAttempts())))
                .toList();

        if (outboxes.isEmpty()) {
            return;
        }

        notificationOutboxRepository.saveAll(outboxes);
    }

    private NotificationChannelSender resolveSender(NotificationChannel channel) {
        Map<NotificationChannel, NotificationChannelSender> senderMap = new EnumMap<>(NotificationChannel.class);
        for (NotificationChannelSender sender : channelSenders) {
            senderMap.put(sender.channel(), sender);
        }

        NotificationChannelSender sender = senderMap.get(channel);
        if (sender == null) {
            throw new IllegalStateException("No notification sender configured for channel " + channel);
        }
        return sender;
    }

    private int resolveWorkerBatchSize() {
        return Math.max(deliveryProperties.getWorkerBatchSize(), 1);
    }

    private int resolveMaxAttempts() {
        return Math.max(deliveryProperties.getMaxAttempts(), 1);
    }

    private Duration resolveProcessingTimeout() {
        Duration timeout = deliveryProperties.getProcessingTimeout();
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            return Duration.ofMinutes(5);
        }
        return timeout;
    }

    private Duration resolveRetryDelay(int attemptCount) {
        Duration initialDelay = deliveryProperties.getInitialRetryDelay();
        Duration maxDelay = deliveryProperties.getMaxRetryDelay();
        long baseDelayMs = initialDelay == null || initialDelay.isNegative() || initialDelay.isZero()
                ? Duration.ofSeconds(30).toMillis()
                : initialDelay.toMillis();
        long cappedMaxDelayMs = maxDelay == null || maxDelay.isNegative() || maxDelay.isZero()
                ? Duration.ofMinutes(15).toMillis()
                : maxDelay.toMillis();
        double multiplier = deliveryProperties.getRetryBackoffMultiplier();
        if (multiplier < 1.0d) {
            multiplier = 1.0d;
        }

        long delayMs = Math.round(baseDelayMs * Math.pow(multiplier, Math.max(attemptCount - 1, 0)));
        delayMs = Math.min(delayMs, cappedMaxDelayMs);
        return Duration.ofMillis(delayMs);
    }

    private <T> T executeInNewTransaction(Supplier<T> supplier) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate.execute(status -> supplier.get());
    }
}
