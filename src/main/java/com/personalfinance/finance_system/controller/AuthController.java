package com.personalfinance.finance_system.controller;

import com.personalfinance.finance_system.dto.AuthRequest;
import com.personalfinance.finance_system.dto.AuthResponse;
import com.personalfinance.finance_system.dto.RegisterRequest;
import com.personalfinance.finance_system.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register new user
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // Login existing user/admin
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // (Optional) Logout endpoint (if token revocation is added later)
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Placeholder – for stateless JWT, logout can be handled on frontend
        return ResponseEntity.ok("Logout successful.");
    }
}
