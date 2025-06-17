package com.personalfinance.finance_system.service.impl;

import com.personalfinance.finance_system.exception.ResourceNotFoundException;
import com.personalfinance.finance_system.model.Expense;
import com.personalfinance.finance_system.model.Limit;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.repository.LimitRepository;
import com.personalfinance.finance_system.repository.ExpenseRepository;
import com.personalfinance.finance_system.repository.UserRepository;
import com.personalfinance.finance_system.service.AdminService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final LimitRepository expenseLimitRepository;

    public AdminServiceImpl(UserRepository userRepository,
                            ExpenseRepository expenseRepository,
                            LimitRepository expenseLimitRepository) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.expenseLimitRepository = expenseLimitRepository;
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();

        // Total expense of all users
        Double totalExpense = expenseRepository.findAll()
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        overview.put("totalExpense", totalExpense);

        // Category-wise expense summary
        Map<String, Double> categorySummary = expenseRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
        overview.put("categorySummary", categorySummary);

        // Optional: You can add other stats or graphs data as needed
        return overview;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<Expense> getUserExpenses(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return expenseRepository.findByUser(user);
    }

    @Override
    public List<User> getUsersExceededLimit() {
        // Get all users who have set a limit
        List<Limit> limits = expenseLimitRepository.findAll();

        List<User> usersExceededLimit = new ArrayList<>();

        for (Limit limit : limits) {
            User user = limit.getUser();
            // Sum of expenses for this user for current month
            Double userExpenseSum = expenseRepository
                    .findByUserAndDateBetween(user,
                            getStartOfCurrentMonth(),
                            getEndOfCurrentMonth())
                    .stream()
                    .mapToDouble(Expense::getAmount)
                    .sum();

            if (userExpenseSum > limit.getLimitAmount()) {
                usersExceededLimit.add(user);
            }
        }
        return usersExceededLimit;
    }

    private LocalDate getStartOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDate getEndOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
