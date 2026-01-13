-- ============================================
-- V2: 유치원 지원 시스템 추가
-- ============================================

-- 1. KINDERGARTEN_APPLICATION (교사 지원 테이블)
CREATE TABLE kindergarten_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL COMMENT '지원 교사 ID',
    kindergarten_id BIGINT NOT NULL COMMENT '대상 유치원 ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED, CANCELLED',
    message TEXT COMMENT '지원 메시지',
    processed_at DATETIME COMMENT '처리 일시',
    rejection_reason VARCHAR(500) COMMENT '거절 사유',
    processed_by BIGINT COMMENT '처리자 (원장) ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,

    CONSTRAINT fk_kindergarten_application_teacher FOREIGN KEY (teacher_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_kindergarten_application_kindergarten FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id) ON DELETE CASCADE,
    CONSTRAINT fk_kindergarten_application_processor FOREIGN KEY (processed_by) REFERENCES member(id) ON DELETE SET NULL,

    INDEX idx_teacher_status (teacher_id, status),
    INDEX idx_kindergarten_status (kindergarten_id, status),
    UNIQUE KEY uk_teacher_kindergarten_active (teacher_id, kindergarten_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='교사 유치원 지원 테이블';

-- 2. KID_APPLICATION (원생 입학 신청 테이블)
CREATE TABLE kid_application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL COMMENT '학부모 ID',
    kindergarten_id BIGINT NOT NULL COMMENT '대상 유치원 ID',
    kid_name VARCHAR(50) NOT NULL COMMENT '원생 이름',
    birth_date DATE NOT NULL COMMENT '생년월일',
    gender VARCHAR(10) NOT NULL COMMENT '성별 (MALE, FEMALE)',
    preferred_classroom_id BIGINT COMMENT '희망 반 ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED, CANCELLED',
    notes TEXT COMMENT '특이사항',
    processed_at DATETIME COMMENT '처리 일시',
    rejection_reason VARCHAR(500) COMMENT '거절 사유',
    processed_by BIGINT COMMENT '처리자 ID',
    kid_id BIGINT COMMENT '승인 후 생성된 Kid ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,

    CONSTRAINT fk_kid_application_parent FOREIGN KEY (parent_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_kid_application_kindergarten FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id) ON DELETE CASCADE,
    CONSTRAINT fk_kid_application_classroom FOREIGN KEY (preferred_classroom_id) REFERENCES classroom(id) ON DELETE SET NULL,
    CONSTRAINT fk_kid_application_processor FOREIGN KEY (processed_by) REFERENCES member(id) ON DELETE SET NULL,
    CONSTRAINT fk_kid_application_kid FOREIGN KEY (kid_id) REFERENCES kid(id) ON DELETE SET NULL,

    INDEX idx_parent_status (parent_id, status),
    INDEX idx_kindergarten_status (kindergarten_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='원생 입학 신청 테이블';

-- 3. NOTIFICATION (알림 테이블)
CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receiver_id BIGINT NOT NULL COMMENT '수신자 ID',
    type VARCHAR(50) NOT NULL COMMENT '알림 타입',
    title VARCHAR(200) NOT NULL COMMENT '제목',
    content TEXT NOT NULL COMMENT '내용',
    link_url VARCHAR(500) COMMENT '링크 URL',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '읽음 여부',
    read_at DATETIME COMMENT '읽은 일시',
    related_entity_type VARCHAR(50) COMMENT '연관 엔티티 타입',
    related_entity_id BIGINT COMMENT '연관 엔티티 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,

    CONSTRAINT fk_notification_receiver FOREIGN KEY (receiver_id) REFERENCES member(id) ON DELETE CASCADE,

    INDEX idx_receiver_read (receiver_id, is_read),
    INDEX idx_receiver_created (receiver_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='알림 테이블';
