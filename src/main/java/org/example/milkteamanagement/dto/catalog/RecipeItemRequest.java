package org.example.milkteamanagement.dto.catalog;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecipeItemRequest(
        @NotNull Long ingredientId,
        @NotNull BigDecimal quantityRequired
) {
}

