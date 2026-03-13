CREATE TABLE member_social_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_member_social_account_member
        FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE UNIQUE INDEX uk_member_social_account_provider_provider_id
    ON member_social_account (provider, provider_id);

CREATE UNIQUE INDEX uk_member_social_account_member_provider
    ON member_social_account (member_id, provider);

CREATE INDEX idx_member_social_account_member_id
    ON member_social_account (member_id);

INSERT INTO member_social_account (member_id, provider, provider_id, created_at, updated_at)
SELECT id, auth_provider, provider_id, created_at, updated_at
FROM member
WHERE auth_provider <> 'LOCAL'
  AND provider_id IS NOT NULL
  AND provider_id <> '';
