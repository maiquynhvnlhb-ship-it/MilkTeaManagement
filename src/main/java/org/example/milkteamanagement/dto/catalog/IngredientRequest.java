package org.example.milkteamanagement.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record IngredientRequest(
        @NotBlank String name,
        @NotBlank String unit,
        @NotNull BigDecimal stockQuantity,
        @NotNull BigDecimal minStockQuantity,
        @NotNull BigDecimal costPerUnit
) {
}

