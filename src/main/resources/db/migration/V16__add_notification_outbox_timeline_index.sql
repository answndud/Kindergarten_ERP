CREATE INDEX idx_notification_outbox_timeline
    ON notification_outbox(status, channel, created_at, id);
