-- ============================================
-- V3: kid_application 중복 방지(학부모-유치원)
-- ============================================

-- 기존 데이터에 중복(parent_id, kindergarten_id)이 있다면 최신 1건만 남기고 나머지는 soft delete 처리
UPDATE kid_application ka
JOIN (
    SELECT parent_id, kindergarten_id, MAX(id) AS keep_id
    FROM kid_application
    WHERE deleted_at IS NULL
    GROUP BY parent_id, kindergarten_id
    HAVING COUNT(*) > 1
) dup
ON ka.parent_id = dup.parent_id
AND ka.kindergarten_id = dup.kindergarten_id
AND ka.id <> dup.keep_id
SET ka.deleted_at = NOW();

-- 동일 유치원에 대한 입학 신청은 1건만 유지 (재신청은 UPDATE로 처리)
ALTER TABLE kid_application
    ADD UNIQUE KEY uk_parent_kindergarten (parent_id, kindergarten_id);
