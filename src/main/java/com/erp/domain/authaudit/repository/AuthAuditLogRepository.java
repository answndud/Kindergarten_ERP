package com.erp.domain.authaudit.repository;

import com.erp.domain.authaudit.entity.AuthAuditLog;
import com.erp.domain.authaudit.entity.AuthAuditEventType;
import com.erp.domain.authaudit.entity.AuthAuditResult;
import com.erp.domain.member.entity.MemberAuthProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, Long> {

    default List<AuthAuditLog> findAllByCreatedAtAsc() {
        return findAll(Sort.by(Sort.Direction.ASC, "createdAt", "id"));
    }

    @Query(value = """
            SELECT log
            FROM AuthAuditLog log, Member member
            WHERE log.memberId = member.id
              AND member.kindergarten.id = :kindergartenId
              AND (:eventType IS NULL OR log.eventType = :eventType)
              AND (:result IS NULL OR log.result = :result)
              AND (:provider IS NULL OR log.provider = :provider)
              AND (:emailKeyword IS NULL OR LOWER(log.email) LIKE LOWER(CONCAT('%', :emailKeyword, '%')))
              AND (:fromCreatedAt IS NULL OR log.createdAt >= :fromCreatedAt)
              AND (:toCreatedAtExclusive IS NULL OR log.createdAt < :toCreatedAtExclusive)
            """,
            countQuery = """
            SELECT COUNT(log)
            FROM AuthAuditLog log, Member member
            WHERE log.memberId = member.id
              AND member.kindergarten.id = :kindergartenId
              AND (:eventType IS NULL OR log.eventType = :eventType)
              AND (:result IS NULL OR log.result = :result)
              AND (:provider IS NULL OR log.provider = :provider)
              AND (:emailKeyword IS NULL OR LOWER(log.email) LIKE LOWER(CONCAT('%', :emailKeyword, '%')))
              AND (:fromCreatedAt IS NULL OR log.createdAt >= :fromCreatedAt)
              AND (:toCreatedAtExclusive IS NULL OR log.createdAt < :toCreatedAtExclusive)
            """)
    Page<AuthAuditLog> searchByKindergartenId(@Param("kindergartenId") Long kindergartenId,
                                              @Param("eventType") AuthAuditEventType eventType,
                                              @Param("result") AuthAuditResult result,
                                              @Param("provider") MemberAuthProvider provider,
                                              @Param("emailKeyword") String emailKeyword,
                                              @Param("fromCreatedAt") LocalDateTime fromCreatedAt,
                                              @Param("toCreatedAtExclusive") LocalDateTime toCreatedAtExclusive,
                                              Pageable pageable);
}
