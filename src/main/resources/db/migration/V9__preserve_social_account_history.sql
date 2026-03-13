ALTER TABLE member_social_account
    ADD COLUMN unlinked_at DATETIME(6) NULL AFTER updated_at;
