-- ============================================
-- V1: 유치원 ERP 초기 스키마
-- ============================================

-- 1. KINDERGARTEN (유치원 테이블) - 먼저 생성
CREATE TABLE kindergarten (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    open_time TIME,
    close_time TIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. MEMBER (회원 테이블) - kindergarten을 참조하므로 나중에 생성
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL COMMENT 'PRINCIPAL, TEACHER, PARENT',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, INACTIVE, PENDING',
    kindergarten_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,

    CONSTRAINT fk_member_kindergarten FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. CLASSROOM (반 테이블)
CREATE TABLE classroom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL COMMENT '반 이름 (예: 해바라기반)',
    age_group VARCHAR(20) COMMENT '연령별 (예: 5세반, 6세반, 7세반)',
    teacher_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,

    CONSTRAINT fk_classroom_kindergarten FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id) ON DELETE CASCADE,
    CONSTRAINT fk_classroom_teacher FOREIGN KEY (teacher_id) REFERENCES member(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. KID (원생 테이블)
CREATE TABLE kid (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL COMMENT 'MALE, FEMALE',
    admission_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,

    CONSTRAINT fk_kid_classroom FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. PARENT_KID (학부모-원생 연결 테이블)
CREATE TABLE parent_kid (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kid_id BIGINT NOT NULL,
    parent_id BIGINT NOT NULL,
    relationship VARCHAR(20) NOT NULL COMMENT 'FATHER, MOTHER, GRANDFATHER, GRANDMOTHER, GUARDIAN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_parent_kid_kid FOREIGN KEY (kid_id) REFERENCES kid(id) ON DELETE CASCADE,
    CONSTRAINT fk_parent_kid_parent FOREIGN KEY (parent_id) REFERENCES member(id) ON DELETE CASCADE,

    UNIQUE KEY uk_parent_kid (parent_id, kid_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. ATTENDANCE (출석 테이블)
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kid_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL COMMENT 'PRESENT, ABSENT, LATE, EARLY_LEAVE, SICK_LEAVE',
    drop_off_time TIME COMMENT '등원 시간',
    pick_up_time TIME COMMENT '하원 시간',
    note VARCHAR(255) COMMENT '결석 사유 등 메모',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_attendance_kid FOREIGN KEY (kid_id) REFERENCES kid(id) ON DELETE CASCADE,

    UNIQUE KEY uk_kid_date (kid_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. NOTEPAD (알림장 테이블)
CREATE TABLE notepad (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT COMMENT '반별 알림장 (null이면 전체)',
    kid_id BIGINT COMMENT '원생별 알림장 (null이면 반 전체)',
    writer_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    photo_url VARCHAR(500) COMMENT '사진 URL (복수일 경우 콤마로 구분)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_notepad_classroom FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
    CONSTRAINT fk_notepad_kid FOREIGN KEY (kid_id) REFERENCES kid(id) ON DELETE CASCADE,
    CONSTRAINT fk_notepad_writer FOREIGN KEY (writer_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. NOTEPAD_READ_CONFIRM (알림장 읽음 확인 테이블)
CREATE TABLE notepad_read_confirm (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notepad_id BIGINT NOT NULL,
    reader_id BIGINT NOT NULL,
    read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_confirm_notepad FOREIGN KEY (notepad_id) REFERENCES notepad(id) ON DELETE CASCADE,
    CONSTRAINT fk_confirm_reader FOREIGN KEY (reader_id) REFERENCES member(id) ON DELETE CASCADE,

    UNIQUE KEY uk_notepad_reader (notepad_id, reader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. ANNOUNCEMENT (공지사항 테이블)
CREATE TABLE announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT NOT NULL,
    writer_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_important BOOLEAN DEFAULT FALSE COMMENT '중요 공지 여부',
    view_count INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,

    CONSTRAINT fk_announcement_kindergarten FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id) ON DELETE CASCADE,
    CONSTRAINT fk_announcement_writer FOREIGN KEY (writer_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스 생성
CREATE INDEX idx_member_email ON member(email);
CREATE INDEX idx_member_kindergarten ON member(kindergarten_id);
CREATE INDEX idx_classroom_kindergarten ON classroom(kindergarten_id);
CREATE INDEX idx_classroom_teacher ON classroom(teacher_id);
CREATE INDEX idx_kid_classroom ON kid(classroom_id);
CREATE INDEX idx_attendance_kid_date ON attendance(kid_id, date);
CREATE INDEX idx_notepad_classroom ON notepad(classroom_id);
CREATE INDEX idx_notepad_kid ON notepad(kid_id);
CREATE INDEX idx_notepad_writer ON notepad(writer_id);
CREATE INDEX idx_announcement_kindergarten ON announcement(kindergarten_id);
CREATE INDEX idx_announcement_created ON announcement(created_at DESC);
