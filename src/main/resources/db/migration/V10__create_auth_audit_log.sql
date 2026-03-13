CREATE TABLE auth_audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NULL,
    email VARCHAR(100) NULL,
    provider VARCHAR(20) NULL,
    event_type VARCHAR(30) NOT NULL,
    result VARCHAR(20) NOT NULL,
    reason VARCHAR(100) NULL,
    client_ip VARCHAR(45) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
);

CREATE INDEX idx_auth_audit_log_created_at
    ON auth_audit_log (created_at);

CREATE INDEX idx_auth_audit_log_member_id_created_at
    ON auth_audit_log (member_id, created_at);

CREATE INDEX idx_auth_audit_log_event_type_created_at
    ON auth_audit_log (event_type, created_at);
