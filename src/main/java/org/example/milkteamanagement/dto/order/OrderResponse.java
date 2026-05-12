package org.example.milkteamanagement.dto.order;

import org.example.milkteamanagement.entity.enums.OrderStatus;

import java.math.BigDecimal;

public record OrderResponse(
        Long orderId,
        String orderCode,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        String deliveryType,
        String customerPhone,
        String customerName,
        String deliveryAddress
) {
}

