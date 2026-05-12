package org.example.milkteamanagement.service;

import org.example.milkteamanagement.dto.order.CheckoutRequest;
import org.example.milkteamanagement.dto.order.CreateOrderRequest;
import org.example.milkteamanagement.dto.order.OrderResponse;
import org.example.milkteamanagement.entity.CustomerOrder;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request, String staffUsername);

    OrderResponse checkout(Long orderId, CheckoutRequest request, String staffUsername);

    OrderResponse sendToKitchen(Long orderId, String staffUsername);

    OrderResponse markPending(Long orderId, String staffUsername);

    OrderResponse markReady(Long orderId, String staffUsername);

    OrderResponse completeOrder(Long orderId, String staffUsername);

    OrderResponse cancel(Long orderId, String actor);

    CustomerOrder findById(Long orderId);

    List<CustomerOrder> findAll();

    List<CustomerOrder> findByStaff(String username);
}


