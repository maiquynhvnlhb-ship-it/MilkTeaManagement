package org.example.milkteamanagement.repository;

import org.example.milkteamanagement.entity.Employee;
import org.example.milkteamanagement.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUserAccount(UserAccount userAccount);
}

