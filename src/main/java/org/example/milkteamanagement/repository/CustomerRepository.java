package org.example.milkteamanagement.repository;

import org.example.milkteamanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {
}

