package org.example.milkteamanagement.dto.auth;

public record AuthResponse(
        String token,
        String username,
        String role
) {
}

