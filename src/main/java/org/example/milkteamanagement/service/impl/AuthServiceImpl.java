package org.example.milkteamanagement.service.impl;

import org.example.milkteamanagement.dto.auth.AuthResponse;
import org.example.milkteamanagement.dto.auth.LoginRequest;
import org.example.milkteamanagement.entity.UserAccount;
import org.example.milkteamanagement.exception.BadRequestException;
import org.example.milkteamanagement.repository.UserAccountRepository;
import org.example.milkteamanagement.security.JwtService;
import org.example.milkteamanagement.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserAccountRepository userAccountRepository,
                           JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        UserAccount user = userAccountRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}

