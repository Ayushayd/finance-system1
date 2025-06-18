package com.personalfinance.finance_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseLimitRequest {

    @NotNull(message = "Limit amount is required")
    @Min(value = 1, message = "Limit must be greater than 0")
    private Double monthlyLimit;
}
