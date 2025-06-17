package com.personalfinance.finance_system.service;

import com.personalfinance.finance_system.dto.ExpenseRequest;
import com.personalfinance.finance_system.dto.ExpenseResponse;

import java.util.List;

public interface ExpenseService {
    ExpenseResponse addExpense(String username, ExpenseRequest request);
    List<ExpenseResponse> getUserExpenses(String username);
    ExpenseResponse getExpenseById(String username, Long id);
    ExpenseResponse updateExpense(String username, Long id, ExpenseRequest request);
    void deleteExpense(String username, Long id);
}
