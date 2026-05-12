package org.example.milkteamanagement.dto.report;

public record TopSellingItemResponse(
        String productName,
        Long quantity
) {
}

