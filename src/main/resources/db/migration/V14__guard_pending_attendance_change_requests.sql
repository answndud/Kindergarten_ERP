ALTER TABLE attendance_change_request
    ADD COLUMN pending_request_date DATE
        GENERATED ALWAYS AS (
            CASE
                WHEN status = 'PENDING' THEN date
                ELSE NULL
            END
        ) STORED COMMENT '동일 원생/날짜의 대기 요청 중복 방지용 생성 컬럼' AFTER status;

ALTER TABLE attendance_change_request
    ADD CONSTRAINT uk_attendance_change_request_pending
        UNIQUE (kid_id, pending_request_date);
