package com.erp.domain.notification.service;

import com.erp.domain.member.entity.Member;
import com.erp.domain.member.repository.MemberRepository;
import com.erp.domain.notification.dto.request.NotificationCreateRequest;
import com.erp.domain.notification.dto.response.NotificationResponse;
import com.erp.domain.notification.dto.response.UnreadCountResponse;
import com.erp.domain.notification.entity.Notification;
import com.erp.domain.notification.entity.NotificationType;
import com.erp.domain.notification.repository.NotificationRepository;
import com.erp.global.exception.BusinessException;
import com.erp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    /**
     * 알림 생성
     */
    @Transactional
    public Long create(NotificationCreateRequest request) {
        Member receiver = memberRepository.findById(request.receiverId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Notification notification;
        if (request.relatedEntityType() != null && request.relatedEntityId() != null) {
            notification = Notification.createWithRelatedEntity(
                    receiver,
                    request.type(),
                    request.title(),
                    request.content(),
                    request.relatedEntityType(),
                    request.relatedEntityId()
            );
        } else if (request.linkUrl() != null) {
            notification = Notification.createWithLink(
                    receiver,
                    request.type(),
                    request.title(),
                    request.content(),
                    request.linkUrl()
            );
        } else {
            notification = Notification.create(
                    receiver,
                    request.type(),
                    request.title(),
                    request.content()
            );
        }

        return notificationRepository.save(notification).getId();
    }

    /**
     * 간편 알림 생성 메서드 (내부용)
     */
    @Transactional
    public Long notify(Long receiverId, NotificationType type, String title, String content) {
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Notification notification = Notification.create(receiver, type, title, content);
        return notificationRepository.save(notification).getId();
    }

    /**
     * 링크 포함 알림 생성 (내부용)
     */
    @Transactional
    public Long notifyWithLink(Long receiverId, NotificationType type, String title, String content, String linkUrl) {
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Notification notification = Notification.createWithLink(receiver, type, title, content, linkUrl);
        return notificationRepository.save(notification).getId();
    }

    /**
     * 연관 엔티티 포함 알림 생성 (내부용)
     */
    @Transactional
    public Long notifyWithRelatedEntity(Long receiverId, NotificationType type, String title, String content,
                                        String relatedEntityType, Long relatedEntityId) {
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Notification notification = Notification.createWithRelatedEntity(
                receiver, type, title, content, relatedEntityType, relatedEntityId
        );
        return notificationRepository.save(notification).getId();
    }

    /**
     * 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long receiverId, int limit) {
        List<Notification> notifications = notificationRepository.findRecentByReceiverId(receiverId);
        if (limit > 0 && notifications.size() > limit) {
            notifications = notifications.subList(0, limit);
        }
        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    /**
     * 알림 상세 조회
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long notificationId, Long receiverId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiver().getId().equals(receiverId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        return NotificationResponse.from(notification);
    }

    /**
     * 알림 읽음 표시
     */
    @Transactional
    public void markAsRead(Long notificationId, Long receiverId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiver().getId().equals(receiverId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        notification.markAsRead();
    }

    /**
     * 전체 읽음 표시
     */
    @Transactional
    public void markAllAsRead(Long receiverId) {
        notificationRepository.markAllAsRead(receiverId, LocalDateTime.now());
    }

    /**
     * 알림 삭제 (Soft Delete)
     */
    @Transactional
    public void delete(Long notificationId, Long receiverId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiver().getId().equals(receiverId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        notification.softDelete();
    }

    /**
     * 안 읽은 알림 개수
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long receiverId) {
        long count = notificationRepository.countUnreadByReceiverId(receiverId);
        return UnreadCountResponse.of(count);
    }

    /**
     * 안 읽은 알림 목록
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long receiverId) {
        List<Notification> notifications = notificationRepository
                .findByReceiverIdAndIsReadFalseAndDeletedAtIsNullOrderByCreatedAtDesc(receiverId);
        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    /**
     * 오래된 읽은 알림 정리 (일괄 작업용)
     */
    @Transactional
    public int cleanupOldReadNotifications(Long receiverId, int daysToKeep) {
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(daysToKeep);
        return notificationRepository.softDeleteOldReadNotifications(receiverId, beforeDate, LocalDateTime.now());
    }
}
