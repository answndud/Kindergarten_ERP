package com.erp.domain.authaudit.repository;

import com.erp.domain.authaudit.entity.AuthAuditLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, Long> {

    default List<AuthAuditLog> findAllByCreatedAtAsc() {
        return findAll(Sort.by(Sort.Direction.ASC, "createdAt", "id"));
    }
}
