package org.example.milkteamanagement.service;

import org.example.milkteamanagement.dto.employee.EmployeeRequest;
import org.example.milkteamanagement.dto.employee.EmployeeResponse;
import org.example.milkteamanagement.entity.enums.EmployeeStatus;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request);

    List<EmployeeResponse> findAll();

    EmployeeResponse update(Long id, EmployeeRequest request);

    EmployeeResponse updateStatus(Long id, EmployeeStatus status, boolean enabled);

    void delete(Long id);
}

