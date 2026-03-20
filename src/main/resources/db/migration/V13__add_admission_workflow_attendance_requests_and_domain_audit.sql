-- ============================================
-- V13: 입학 워크플로우/출결 요청/업무 감사 로그 고도화
-- ============================================

ALTER TABLE classroom
    ADD COLUMN capacity INT NOT NULL DEFAULT 20 COMMENT '반 정원' AFTER age_group;

ALTER TABLE kid_application
    ADD COLUMN assigned_classroom_id BIGINT NULL COMMENT '실제 배정/offer 대상 반 ID' AFTER preferred_classroom_id,
    ADD COLUMN waitlisted_at DATETIME NULL COMMENT '대기열 등록 시각' AFTER processed_at,
    ADD COLUMN offered_at DATETIME NULL COMMENT '입학 offer 시각' AFTER waitlisted_at,
    ADD COLUMN offer_expires_at DATETIME NULL COMMENT '입학 offer 만료 시각' AFTER offered_at,
    ADD COLUMN offer_accepted_at DATETIME NULL COMMENT '학부모 offer 수락 시각' AFTER offer_expires_at,
    ADD COLUMN decision_note VARCHAR(500) NULL COMMENT '대기열/offer 처리 메모' AFTER rejection_reason;

ALTER TABLE kid_application
    ADD CONSTRAINT fk_kid_application_assigned_classroom
        FOREIGN KEY (assigned_classroom_id) REFERENCES classroom(id) ON DELETE SET NULL;

CREATE INDEX idx_kid_application_assigned_status
    ON kid_application (assigned_classroom_id, status, created_at);

CREATE INDEX idx_kid_application_offer_expiry
    ON kid_application (status, offer_expires_at);

CREATE TABLE attendance_change_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL COMMENT '요청 당시 유치원 ID',
    classroom_id BIGINT NOT NULL COMMENT '요청 당시 반 ID',
    kid_id BIGINT NOT NULL COMMENT '대상 원생 ID',
    requester_id BIGINT NOT NULL COMMENT '요청한 학부모 ID',
    reviewed_by BIGINT NULL COMMENT '검토한 교사/원장 ID',
    attendance_id BIGINT NULL COMMENT '승인 후 반영된 출결 ID',
    date DATE NOT NULL COMMENT '출결 대상 날짜',
    requested_status VARCHAR(20) NOT NULL COMMENT '요청 출결 상태',
    requested_drop_off_time TIME NULL COMMENT '요청 등원 시간',
    requested_pick_up_time TIME NULL COMMENT '요청 하원 시간',
    note VARCHAR(255) NULL COMMENT '요청 메모',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED, CANCELLED',
    rejection_reason VARCHAR(500) NULL COMMENT '거절 사유',
    reviewed_at DATETIME NULL COMMENT '승인/거절 시각',
    cancelled_at DATETIME NULL COMMENT '요청 취소 시각',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_attendance_change_request_kindergarten
        FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_change_request_classroom
        FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_change_request_kid
        FOREIGN KEY (kid_id) REFERENCES kid(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_change_request_requester
        FOREIGN KEY (requester_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_change_request_reviewer
        FOREIGN KEY (reviewed_by) REFERENCES member(id) ON DELETE SET NULL,
    CONSTRAINT fk_attendance_change_request_attendance
        FOREIGN KEY (attendance_id) REFERENCES attendance(id) ON DELETE SET NULL,

    INDEX idx_attendance_change_request_requester_created (requester_id, created_at DESC),
    INDEX idx_attendance_change_request_status_created (kindergarten_id, status, created_at DESC),
    INDEX idx_attendance_change_request_classroom_status_date (classroom_id, status, date),
    INDEX idx_attendance_change_request_kid_date_status (kid_id, date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='학부모 출결 변경 요청 테이블';

CREATE TABLE domain_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL COMMENT '유치원 ID',
    actor_id BIGINT NULL COMMENT '행위자 member ID',
    actor_name VARCHAR(50) NULL COMMENT '행위자 이름',
    actor_role VARCHAR(20) NULL COMMENT '행위자 역할',
    action VARCHAR(100) NOT NULL COMMENT '업무 감사 액션',
    target_type VARCHAR(50) NOT NULL COMMENT '대상 타입',
    target_id BIGINT NULL COMMENT '대상 엔티티 ID',
    summary VARCHAR(255) NOT NULL COMMENT '사람이 읽는 요약',
    metadata_json TEXT NULL COMMENT '부가 메타데이터 JSON',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_domain_audit_log_kindergarten_created (kindergarten_id, created_at DESC),
    INDEX idx_domain_audit_log_kindergarten_action_created (kindergarten_id, action, created_at DESC),
    INDEX idx_domain_audit_log_kindergarten_target_created (kindergarten_id, target_type, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='업무 상태 변경 감사 로그';
