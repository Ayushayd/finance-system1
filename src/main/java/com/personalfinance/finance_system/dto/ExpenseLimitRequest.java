package com.personalfinance.finance_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExpenseLimitRequest {

    // Getters and Setters
    @NotNull(message = "Limit amount is required")
    @Min(value = 0, message = "Limit must be positive")
    private Double limitAmount;

}
