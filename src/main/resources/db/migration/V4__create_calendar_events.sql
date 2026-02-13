-- ============================================
-- V4: Calendar events
-- ============================================

CREATE TABLE calendar_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kindergarten_id BIGINT,
    classroom_id BIGINT,
    creator_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    start_datetime DATETIME NOT NULL,
    end_datetime DATETIME NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    scope_type VARCHAR(20) NOT NULL,
    is_all_day BOOLEAN NOT NULL DEFAULT FALSE,
    location VARCHAR(200),
    repeat_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
    repeat_end_date DATE DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,

    CONSTRAINT fk_calendar_event_kindergarten FOREIGN KEY (kindergarten_id) REFERENCES kindergarten(id) ON DELETE SET NULL,
    CONSTRAINT fk_calendar_event_classroom FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE SET NULL,
    CONSTRAINT fk_calendar_event_creator FOREIGN KEY (creator_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_calendar_event_kindergarten ON calendar_event(kindergarten_id);
CREATE INDEX idx_calendar_event_classroom ON calendar_event(classroom_id);
CREATE INDEX idx_calendar_event_creator ON calendar_event(creator_id);
CREATE INDEX idx_calendar_event_range ON calendar_event(start_datetime, end_datetime);
