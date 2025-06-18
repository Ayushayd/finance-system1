package com.personalfinance.finance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExpenseLimitResponse {
    private Double monthlyLimit;
}
