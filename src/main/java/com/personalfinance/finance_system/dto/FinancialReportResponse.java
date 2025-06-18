package com.personalfinance.finance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class FinancialReportResponse {
    private Double totalIncome;
    private Double totalExpense;
    private Double balance;
    private Map<String, Double> expenseByCategory;
}
