package org.example.milkteamanagement.dto.order;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderItemRequest(
        @NotNull Long productId,
        @NotNull Integer quantity,
        List<ToppingRequest> toppings
) {
}

