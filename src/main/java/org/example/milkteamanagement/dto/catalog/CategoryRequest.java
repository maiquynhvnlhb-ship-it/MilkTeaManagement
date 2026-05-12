package org.example.milkteamanagement.dto.catalog;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank String name,
        String description,
        boolean active
) {
}

