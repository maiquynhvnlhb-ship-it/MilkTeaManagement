package org.example.milkteamanagement.repository;

import org.example.milkteamanagement.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}

