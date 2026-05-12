package org.example.milkteamanagement.service.impl;

import org.example.milkteamanagement.dto.employee.EmployeeRequest;
import org.example.milkteamanagement.dto.employee.EmployeeResponse;
import org.example.milkteamanagement.entity.Employee;
import org.example.milkteamanagement.entity.UserAccount;
import org.example.milkteamanagement.entity.enums.EmployeeStatus;
import org.example.milkteamanagement.entity.enums.RoleName;
import org.example.milkteamanagement.exception.NotFoundException;
import org.example.milkteamanagement.repository.EmployeeRepository;
import org.example.milkteamanagement.repository.UserAccountRepository;
import org.example.milkteamanagement.service.EmployeeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               UserAccountRepository userAccountRepository,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        UserAccount account = new UserAccount();
        account.setUsername(request.username());
        account.setPassword(passwordEncoder.encode(request.password()));
        account.setRole(parseRole(request.role()));
        account.setEnabled(true);
        userAccountRepository.save(account);

        Employee employee = new Employee();
        employee.setFullName(request.fullName());
        employee.setPhone(request.phone());
        employee.setStatus(EmployeeStatus.WORKING);
        employee.setUserAccount(account);
        employeeRepository.save(employee);
        return toResponse(employee);
    }

    @Override
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        employee.setFullName(request.fullName());
        employee.setPhone(request.phone());
        if (request.role() != null && !request.role().isBlank()) {
            employee.getUserAccount().setRole(parseRole(request.role()));
        }
        if (request.password() != null && !request.password().isBlank()) {
            employee.getUserAccount().setPassword(passwordEncoder.encode(request.password()));
        }
        return toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateStatus(Long id, EmployeeStatus status, boolean enabled) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        employee.setStatus(status);
        employee.getUserAccount().setEnabled(enabled);
        return toResponse(employee);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found: " + id));
        employee.getUserAccount().setEnabled(false);
        employee.setStatus(EmployeeStatus.QUIT);
    }

    private EmployeeResponse toResponse(Employee employee) {
        UserAccount account = employee.getUserAccount();
        return new EmployeeResponse(
                employee.getId(),
                employee.getFullName(),
                employee.getPhone(),
                employee.getStatus(),
                account.getUsername(),
                account.getRole(),
                account.isEnabled()
        );
    }

    private RoleName parseRole(String raw) {
        if (raw == null || raw.isBlank()) {
            return RoleName.STAFF;
        }
        return RoleName.valueOf(raw.toUpperCase());
    }
}

