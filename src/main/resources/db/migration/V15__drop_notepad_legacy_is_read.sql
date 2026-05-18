-- V15: Remove legacy notepad read flag.
-- Actual read state is tracked by notepad_read_confirm.
ALTER TABLE notepad
    DROP COLUMN is_read;
