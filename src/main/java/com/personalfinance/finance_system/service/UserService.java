package com.personalfinance.finance_system.service;

import com.personalfinance.finance_system.dto.ExpenseLimitRequest;
import com.personalfinance.finance_system.dto.ExpenseRequest;
import com.personalfinance.finance_system.dto.FinancialReportResponse;
import com.personalfinance.finance_system.dto.IncomeRequest;
import com.personalfinance.finance_system.model.Expense;
import com.personalfinance.finance_system.model.Income;
import com.personalfinance.finance_system.model.Limit;

import java.util.List;
import java.util.Map;

public interface UserService {

    Map<String, Object> getOverview(String username);

    Map<String, Object> getChartData(String username);

    Limit setLimit(String username, ExpenseLimitRequest request);

    Limit getLimit(String username);
    Expense addExpense(String username, ExpenseRequest request);
    List<Expense> getAllExpenses(String username);
    Income addIncome(String username, IncomeRequest request);
    FinancialReportResponse getFinancialReport(String username);
}
