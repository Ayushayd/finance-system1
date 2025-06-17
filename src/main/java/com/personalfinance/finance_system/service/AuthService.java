package com.personalfinance.finance_system.service;

import com.personalfinance.finance_system.dto.AuthRequest;
import com.personalfinance.finance_system.dto.AuthResponse;
import com.personalfinance.finance_system.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
}