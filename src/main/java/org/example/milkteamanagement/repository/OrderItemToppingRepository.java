package org.example.milkteamanagement.repository;

import org.example.milkteamanagement.entity.OrderItem;
import org.example.milkteamanagement.entity.OrderItemTopping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemToppingRepository extends JpaRepository<OrderItemTopping, Long> {
	List<OrderItemTopping> findByOrderItemIn(List<OrderItem> items);
}


