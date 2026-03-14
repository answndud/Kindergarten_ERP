ALTER TABLE auth_audit_log
    ADD COLUMN kindergarten_id BIGINT NULL AFTER member_id;

UPDATE auth_audit_log log
JOIN member m ON log.member_id = m.id
SET log.kindergarten_id = m.kindergarten_id
WHERE log.member_id IS NOT NULL
  AND log.kindergarten_id IS NULL
  AND m.kindergarten_id IS NOT NULL;

CREATE INDEX idx_auth_audit_log_kindergarten_created_at
    ON auth_audit_log (kindergarten_id, created_at, id);

CREATE INDEX idx_auth_audit_log_kindergarten_event_result_created_at
    ON auth_audit_log (kindergarten_id, event_type, result, created_at);

CREATE TABLE auth_audit_log_archive (
    id BIGINT NOT NULL,
    member_id BIGINT NULL,
    kindergarten_id BIGINT NULL,
    email VARCHAR(100) NULL,
    provider VARCHAR(20) NULL,
    event_type VARCHAR(30) NOT NULL,
    result VARCHAR(20) NOT NULL,
    reason VARCHAR(100) NULL,
    client_ip VARCHAR(45) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    archived_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_auth_audit_log_archive_kindergarten_created_at
    ON auth_audit_log_archive (kindergarten_id, created_at, id);

CREATE INDEX idx_auth_audit_log_archive_archived_at
    ON auth_audit_log_archive (archived_at);
