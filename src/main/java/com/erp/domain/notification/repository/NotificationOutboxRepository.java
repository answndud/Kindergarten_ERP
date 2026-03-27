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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = """
            SELECT id
            FROM notification_outbox
            WHERE status = 'PENDING'
              AND next_attempt_at <= :now
            ORDER BY next_attempt_at ASC, id ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Long> claimPendingIds(@Param("now") LocalDateTime now, @Param("limit") int limit);

    @Query(value = """
            SELECT id
            FROM notification_outbox
            WHERE status = 'PROCESSING'
              AND processing_started_at <= :staleBefore
            ORDER BY processing_started_at ASC, id ASC
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Long> claimStaleProcessingIds(@Param("staleBefore") LocalDateTime staleBefore, @Param("limit") int limit);

    long countByStatus(NotificationDeliveryStatus status);

    long countByStatusAndChannel(NotificationDeliveryStatus status, NotificationChannel channel);

    long countByNotificationIdAndStatusIn(Long notificationId, Collection<NotificationDeliveryStatus> statuses);

    List<NotificationOutbox> findByNotificationIdOrderByIdAsc(Long notificationId);

    List<NotificationOutbox> findByIdIn(Collection<Long> ids);

    Optional<NotificationOutbox> findByNotificationIdAndChannel(Long notificationId, NotificationChannel channel);

    List<NotificationOutbox> findByChannelOrderByIdAsc(NotificationChannel channel);
}
