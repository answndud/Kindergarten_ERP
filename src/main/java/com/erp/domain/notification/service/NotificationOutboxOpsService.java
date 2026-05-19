package com.erp.domain.notification.service;

import com.erp.domain.notification.config.NotificationDeliveryProperties;
import com.erp.domain.notification.dto.response.NotificationOutboxItemResponse;
import com.erp.domain.notification.dto.response.NotificationOutboxSummaryResponse;
import com.erp.domain.notification.entity.NotificationDeliveryStatus;
import com.erp.domain.notification.entity.NotificationOutbox;
import com.erp.domain.notification.repository.NotificationOutboxRepository;
import com.erp.domain.notification.service.channel.NotificationChannel;
import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationOutboxOpsService {

    private static final int MAX_DEAD_LETTER_LIMIT = 100;

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotificationDeliveryProperties deliveryProperties;

    public NotificationOutboxSummaryResponse getSummary() {
        Map<String, Long> statusCounts = Arrays.stream(NotificationDeliveryStatus.values())
                .collect(Collectors.toMap(Enum::name, notificationOutboxRepository::countByStatus));
        Map<String, Long> deadLetterCountsByChannel = Arrays.stream(NotificationChannel.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        channel -> notificationOutboxRepository.countByStatusAndChannel(
                                NotificationDeliveryStatus.DEAD_LETTER,
                                channel
                        )
                ));

        return new NotificationOutboxSummaryResponse(statusCounts, deadLetterCountsByChannel);
    }

    public Page<NotificationOutboxItemResponse> getDeadLetters(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_DEAD_LETTER_LIMIT);
        return notificationOutboxRepository.findByStatusOrderByDeadLetteredAtDescIdDesc(
                        NotificationDeliveryStatus.DEAD_LETTER,
                        PageRequest.of(safePage, safeSize)
                )
                .map(NotificationOutboxItemResponse::from);
    }

    @Transactional
    public NotificationOutboxItemResponse retryDeadLetter(Long outboxId) {
        NotificationOutbox outbox = notificationOutboxRepository.findById(outboxId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (outbox.getStatus() != NotificationDeliveryStatus.DEAD_LETTER) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "dead-letter 상태의 outbox만 재시도할 수 있습니다");
        }

        outbox.resetDeadLetterForRetry(LocalDateTime.now(), deliveryProperties.getMaxAttempts());
        return NotificationOutboxItemResponse.from(outbox);
    }
}
