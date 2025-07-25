package com.personalfinance.finance_system.service;

import com.personalfinance.finance_system.dto.*;
import com.personalfinance.finance_system.model.Expense;
import com.personalfinance.finance_system.model.Income;
import com.personalfinance.finance_system.model.Limit;
import com.personalfinance.finance_system.model.User;

import java.util.List;
import java.util.Map;

public interface AdminService {

    // Get overall dashboard stats and graphs (e.g., total expenses, category-wise data)
    Map<String, Object> getOverview();

    // List all users in the system
    List<User> getAllUsers();

    // Get all expenses for a particular user by userId
    List<Expense> getUserExpenses(Long userId);
    List<ExpenseResponse> getAllExpenses();

    List<IncomeWithUserLimitDTO> getAllIncomes();


    ExpenseLimitResponse setExpenseLimit(Long userId, Double monthlyLimit);

    // Get list of users who have exceeded their expense limit
    List<User> getUsersExceededLimit();

    Map<String, Double> getExpensesGroupedByCategory();
    FinancialReportResponse getSystemFinancialReport();

}
