CREATE TABLE notification_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    channel VARCHAR(50) NOT NULL,
    receiver_member_id BIGINT NULL,
    receiver_email VARCHAR(255) NULL,
    receiver_name VARCHAR(100) NULL,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    link_url VARCHAR(500) NULL,
    status VARCHAR(30) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 5,
    next_attempt_at DATETIME NOT NULL,
    processing_started_at DATETIME NULL,
    last_attempt_at DATETIME NULL,
    delivered_at DATETIME NULL,
    dead_lettered_at DATETIME NULL,
    last_error VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_outbox_notification
        FOREIGN KEY (notification_id) REFERENCES notification(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notification_outbox_status_next_attempt
    ON notification_outbox(status, next_attempt_at, id);

CREATE INDEX idx_notification_outbox_processing_started
    ON notification_outbox(status, processing_started_at, id);

CREATE INDEX idx_notification_outbox_notification
    ON notification_outbox(notification_id);

ALTER TABLE notification_outbox
    ADD CONSTRAINT uk_notification_outbox_notification_channel
        UNIQUE (notification_id, channel);
