package com.personalfinance.finance_system.service.impl;

import com.personalfinance.finance_system.dto.ExpenseLimitRequest;
import com.personalfinance.finance_system.dto.ExpenseRequest;
import com.personalfinance.finance_system.dto.IncomeRequest;
import com.personalfinance.finance_system.dto.FinancialReportResponse;
import com.personalfinance.finance_system.exception.ResourceNotFoundException;
import com.personalfinance.finance_system.model.*;
import com.personalfinance.finance_system.repository.*;

import com.personalfinance.finance_system.service.UserService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final LimitRepository expenseLimitRepository;

    public UserServiceImpl(UserRepository userRepository,
                           ExpenseRepository expenseRepository,
                           IncomeRepository incomeRepository,
                           LimitRepository expenseLimitRepository) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.expenseLimitRepository = expenseLimitRepository;
    }

    @Override
    public Map<String, Object> getOverview(String username) {
        User user = getUser(username);
        List<Expense> expenses = expenseRepository.findByUser(user);

        double totalExpense = expenses.stream().mapToDouble(Expense::getAmount).sum();

        Map<String, Double> categorySummary = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalExpense", totalExpense);
        overview.put("categorySummary", categorySummary);
        return overview;
    }

    @Override
    public Map<String, Object> getChartData(String username) {
        User user = getUser(username);
        List<Expense> expenses = expenseRepository.findByUser(user);

        Map<String, Double> monthlyExpense = new TreeMap<>();

        expenses.forEach(expense -> {
            if (expense.getDate() != null) {
                String monthKey = expense.getDate().getYear() + "-" +
                        String.format("%02d", expense.getDate().getMonthValue());
                monthlyExpense.put(monthKey,
                        monthlyExpense.getOrDefault(monthKey, 0.0) + expense.getAmount());
            } else {
                // Optional: Log or handle null date
                System.out.println("Skipping expense with null date: " + expense.getId());
            }
        });

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("monthlyExpense", monthlyExpense);
        return chartData;
    }


    @Override
    public Limit setLimit(String username, ExpenseLimitRequest request) {
        User user = getUser(username);
        Optional<Limit> existingLimitOpt = expenseLimitRepository.findByUser(user);

        Limit expenseLimit = existingLimitOpt.orElseGet(Limit::new);
        expenseLimit.setUser(user);
        expenseLimit.setLimitAmount(request.getMonthlyLimit());

        return expenseLimitRepository.save(expenseLimit);
    }

    @Override
    public Limit getLimit(String username) {
        User user = getUser(username);
        return expenseLimitRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Expense limit not found for user: " + username));
    }

    @Override
    public Expense addExpense(String username, ExpenseRequest request) {
        User user = getUser(username);
        Expense expense = new Expense();
        expense.setUser(user);
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription());
        return expenseRepository.save(expense);
    }

    @Override
    public List<Expense> getAllExpenses(String username) {
        User user = getUser(username);
        return expenseRepository.findByUser(user);
    }

    @Override
    public Income addIncome(String username, IncomeRequest request) {
        User user = getUser(username);
        Income income = new Income();
        income.setUser(user);
        income.setAmount(request.getAmount());
        income.setSource(request.getSource());
        income.setDate(request.getDate());
        return incomeRepository.save(income);
    }

    @Override
    public FinancialReportResponse getFinancialReport(String username) {
        User user = getUser(username);
        List<Expense> expenses = expenseRepository.findByUser(user);
        List<Income> incomes = incomeRepository.findByUser(user);

        double totalExpense = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double totalIncome = incomes.stream().mapToDouble(Income::getAmount).sum();
        double balance = totalIncome - totalExpense;

        Map<String, Double> expenseByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        return new FinancialReportResponse(totalIncome, totalExpense, balance, expenseByCategory);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }
}
