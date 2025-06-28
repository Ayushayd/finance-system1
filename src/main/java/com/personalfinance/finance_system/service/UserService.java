package com.personalfinance.finance_system.service;

import com.personalfinance.finance_system.dto.*;
import com.personalfinance.finance_system.model.Expense;
import com.personalfinance.finance_system.model.Income;
import com.personalfinance.finance_system.model.Limit;
import com.personalfinance.finance_system.model.User;

import java.util.List;
import java.util.Map;

public interface UserService {

    Map<String, Object> getOverview(String username);

    Map<String, Object> getChartData(String username);

    Limit setLimit(String username, ExpenseLimitRequest request);
    ExpenseLimitResponse getLimit(String username);


    Expense addExpense(String username, ExpenseRequest request);
    List<ExpenseResponse> getAllExpenses(String username);

    Income addIncome(String username, IncomeRequest request);
    List<IncomeResponseDTO> getIncome(String username);
    IncomeResponseDTO updateIncome(String username, Long incomeId, IncomeRequest request);
    void deleteIncome(String username, Long incomeId);

    FinancialReportResponse getFinancialReport(String username);

    User getUser(String username);

}
