package org.example.milkteamanagement.dto.employee;

import org.example.milkteamanagement.entity.enums.EmployeeStatus;
import org.example.milkteamanagement.entity.enums.RoleName;

public record EmployeeResponse(
        Long id,
        String fullName,
        String phone,
        EmployeeStatus status,
        String username,
        RoleName role,
        boolean enabled
) {
}

