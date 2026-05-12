package org.example.milkteamanagement.controller.admin;

import org.example.milkteamanagement.entity.AuditLog;
import org.example.milkteamanagement.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/system")
public class AdminSystemController {

    private final AuditLogRepository auditLogRepository;

    public AdminSystemController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> auditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }
}

