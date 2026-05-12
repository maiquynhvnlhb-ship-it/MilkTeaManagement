package org.example.milkteamanagement.controller.admin;

import jakarta.validation.Valid;
import org.example.milkteamanagement.dto.employee.EmployeeRequest;
import org.example.milkteamanagement.dto.employee.EmployeeResponse;
import org.example.milkteamanagement.entity.enums.EmployeeStatus;
import org.example.milkteamanagement.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/employees")
public class AdminEmployeeController {

    private final EmployeeService employeeService;

    public AdminEmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAll() {
        return ResponseEntity.ok(employeeService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EmployeeResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        EmployeeStatus status = EmployeeStatus.valueOf(request.getOrDefault("status", "WORKING").toUpperCase());
        boolean enabled = Boolean.parseBoolean(request.getOrDefault("enabled", "true"));
        return ResponseEntity.ok(employeeService.updateStatus(id, status, enabled));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
