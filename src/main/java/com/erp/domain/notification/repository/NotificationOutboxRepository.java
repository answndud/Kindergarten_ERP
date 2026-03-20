package com.erp.domain.notification.repository;

import com.erp.domain.notification.entity.NotificationDeliveryStatus;
import com.erp.domain.notification.entity.NotificationOutbox;
import com.erp.domain.notification.service.channel.NotificationChannel;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    List<NotificationOutbox> findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAscIdAsc(
            NotificationDeliveryStatus status,
            LocalDateTime now,
            Pageable pageable
    );

    List<NotificationOutbox> findByStatusAndProcessingStartedAtLessThanEqualOrderByProcessingStartedAtAscIdAsc(
            NotificationDeliveryStatus status,
            LocalDateTime staleBefore,
            Pageable pageable
    );

    long countByStatus(NotificationDeliveryStatus status);

    long countByStatusAndChannel(NotificationDeliveryStatus status, NotificationChannel channel);

    long countByNotificationIdAndStatusIn(Long notificationId, Collection<NotificationDeliveryStatus> statuses);

    List<NotificationOutbox> findByNotificationIdOrderByIdAsc(Long notificationId);

    Optional<NotificationOutbox> findByNotificationIdAndChannel(Long notificationId, NotificationChannel channel);

    List<NotificationOutbox> findByChannelOrderByIdAsc(NotificationChannel channel);
}
