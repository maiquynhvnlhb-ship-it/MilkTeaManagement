package org.example.milkteamanagement.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank String name,
        @NotNull Long categoryId,
        @NotNull BigDecimal price,
        String status,
        boolean topping
) {
}

