package com.personalfinance.finance_system.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthResponse {

    // Getter and Setter
    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }

}
