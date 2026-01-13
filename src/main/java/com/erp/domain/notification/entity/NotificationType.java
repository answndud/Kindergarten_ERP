package com.erp.domain.notification.entity;

/**
 * 알림 타입
 */
public enum NotificationType {
    KINDERGARTEN_APPLICATION_SUBMITTED("교사 지원 신청"),
    KINDERGARTEN_APPLICATION_APPROVED("교사 지원 승인"),
    KINDERGARTEN_APPLICATION_REJECTED("교사 지원 거절"),
    KINDERGARTEN_APPLICATION_CANCELLED("교사 지원 취소"),
    KID_APPLICATION_SUBMITTED("원생 입학 신청"),
    KID_APPLICATION_APPROVED("원생 입학 승인"),
    KID_APPLICATION_REJECTED("원생 입학 거절"),
    KID_APPLICATION_CANCELLED("원생 입학 취소"),
    NOTEPAD_CREATED("알림장 작성"),
    NOTEPAD_READ_CONFIRM("알림장 읽음 확인"),
    ANNOUNCEMENT_CREATED("공지사항 작성"),
    SYSTEM("시스템 알림");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
