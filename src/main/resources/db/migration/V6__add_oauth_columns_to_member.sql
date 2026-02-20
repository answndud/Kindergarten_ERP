ALTER TABLE member
    MODIFY COLUMN password VARCHAR(255) NULL,
    ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL' AFTER password,
    ADD COLUMN provider_id VARCHAR(100) NULL AFTER auth_provider;

CREATE UNIQUE INDEX uk_member_auth_provider_provider_id
    ON member (auth_provider, provider_id);
