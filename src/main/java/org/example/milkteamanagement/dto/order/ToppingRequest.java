package org.example.milkteamanagement.dto.order;

import jakarta.validation.constraints.NotNull;

public record ToppingRequest(
        @NotNull Long productId,
        @NotNull Integer quantity
) {
}

