package org.example.milkteamanagement.service;

import org.example.milkteamanagement.dto.auth.AuthResponse;
import org.example.milkteamanagement.dto.auth.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}

