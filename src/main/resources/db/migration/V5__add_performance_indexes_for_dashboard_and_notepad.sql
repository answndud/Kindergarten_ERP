-- ============================================
-- V5: Dashboard/Notepad 성능 인덱스 추가
-- ============================================

-- 1) Notepad 목록 조회 최적화
--    - 반별 목록: classroom_id + kid_id(null) + created_at 정렬
--    - 원생별 목록: kid_id + created_at 정렬
CREATE INDEX idx_notepad_classroom_kid_created
    ON notepad (classroom_id, kid_id, created_at DESC);

CREATE INDEX idx_notepad_kid_created
    ON notepad (kid_id, created_at DESC);

-- 2) Announcement 목록 정렬 최적화
--    - 유치원 + 삭제여부 필터 + 중요도/생성일 정렬
CREATE INDEX idx_announcement_kind_deleted_important_created
    ON announcement (kindergarten_id, deleted_at, is_important DESC, created_at DESC);

-- 3) Dashboard 출석 통계 집계 최적화
--    - date range + status 필터 후 kid 조인
CREATE INDEX idx_attendance_date_status_kid
    ON attendance (date, status, kid_id);
