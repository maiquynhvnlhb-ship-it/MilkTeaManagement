package org.example.milkteamanagement.dto.order;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequest(
        @NotBlank String paymentMethod,
        // frontend can pass true to auto complete order when payment is done (e.g., pickup immediate)
        Boolean autoCompleteOnPaid
) {
}

