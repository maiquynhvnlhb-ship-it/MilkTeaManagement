package org.example.milkteamanagement.repository;

import org.example.milkteamanagement.entity.PaymentTransaction;
import org.example.milkteamanagement.entity.CustomerOrder;
import org.example.milkteamanagement.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
	List<PaymentTransaction> findByOrderAndStatus(CustomerOrder order, PaymentStatus status);
}

