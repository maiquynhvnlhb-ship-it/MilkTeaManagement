package org.example.milkteamanagement.dto.employee;

import jakarta.validation.constraints.NotBlank;

public record EmployeeRequest(
        @NotBlank String fullName,
        String phone,
        @NotBlank String username,
        @NotBlank String password,
        String role
) {
}

