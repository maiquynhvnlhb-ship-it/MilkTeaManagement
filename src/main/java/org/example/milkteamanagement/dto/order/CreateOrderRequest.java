package org.example.milkteamanagement.dto.order;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty List<OrderItemRequest> items,
        String voucherCode,
        String note,
        String deliveryType, // PICKUP or DELIVERY
        String customerName,
        String customerPhone,
        String deliveryAddress
) {
}

