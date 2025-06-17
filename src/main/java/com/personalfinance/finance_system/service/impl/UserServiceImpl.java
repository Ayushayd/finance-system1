package com.personalfinance.finance_system.service.impl;

import com.personalfinance.finance_system.dto.ExpenseLimitRequest;
import com.personalfinance.finance_system.exception.ResourceNotFoundException;
import com.personalfinance.finance_system.model.Expense;
import com.personalfinance.finance_system.model.Limit;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.repository.LimitRepository;
import com.personalfinance.finance_system.repository.ExpenseRepository;
import com.personalfinance.finance_system.repository.UserRepository;
import com.personalfinance.finance_system.service.UserService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final LimitRepository expenseLimitRepository;

    public UserServiceImpl(UserRepository userRepository,
                           ExpenseRepository expenseRepository,
                           LimitRepository expenseLimitRepository) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.expenseLimitRepository = expenseLimitRepository;
    }

    @Override
    public Map<String, Object> getOverview(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        List<Expense> expenses = expenseRepository.findByUser(user);

        double totalExpense = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        List<Expense> expenses = expenseRepository.findByUser(user);

        Map<String, Double> monthlyExpense = new TreeMap<>(); // Sorted by month string "YYYY-MM"

        expenses.forEach(expense -> {
            String monthKey = expense.getDate().getYear() + "-" + String.format("%02d", expense.getDate().getMonthValue());
            monthlyExpense.put(monthKey,
                    monthlyExpense.getOrDefault(monthKey, 0.0) + expense.getAmount());
        });

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("monthlyExpense", monthlyExpense);
        return chartData;
    }


    @Override
    public Limit setLimit(String username, ExpenseLimitRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Optional<Limit> existingLimitOpt = expenseLimitRepository.findByUser(user);
        Limit expenseLimit;

        if (existingLimitOpt.isPresent()) {
            expenseLimit = existingLimitOpt.get();
            expenseLimit.setLimitAmount(request.getLimitAmount());
        } else {
            expenseLimit = new Limit();
            expenseLimit.setUser(user);
            expenseLimit.setLimitAmount(request.getLimitAmount());
        }

        return expenseLimitRepository.save(expenseLimit);
    }

    @Override
    public Limit getLimit(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return expenseLimitRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Expense limit not found for user: " + username));
    }
}
