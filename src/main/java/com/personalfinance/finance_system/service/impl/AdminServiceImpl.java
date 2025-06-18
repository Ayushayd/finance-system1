package com.personalfinance.finance_system.service.impl;

import com.personalfinance.finance_system.dto.*;
import com.personalfinance.finance_system.exception.ResourceNotFoundException;
import com.personalfinance.finance_system.model.Expense;
import com.personalfinance.finance_system.model.Income;
import com.personalfinance.finance_system.model.Limit;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.repository.ExpenseRepository;
import com.personalfinance.finance_system.repository.IncomeRepository;
import com.personalfinance.finance_system.repository.LimitRepository;
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
    private final IncomeRepository incomeRepository;

    public AdminServiceImpl(UserRepository userRepository,
                            ExpenseRepository expenseRepository,
                            LimitRepository expenseLimitRepository,
                            IncomeRepository incomeRepository) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.expenseLimitRepository = expenseLimitRepository;
        this.incomeRepository = incomeRepository;
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
        Map<String, Double> categorySummary = getExpensesGroupedByCategory();
        overview.put("categorySummary", categorySummary);

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
        List<Limit> limits = expenseLimitRepository.findAll();
        List<User> usersExceededLimit = new ArrayList<>();

        for (Limit limit : limits) {
            User user = limit.getUser();

            Double userExpenseSum = expenseRepository
                    .findByUserAndDateBetween(user, getStartOfCurrentMonth(), getEndOfCurrentMonth())
                    .stream()
                    .mapToDouble(Expense::getAmount)
                    .sum();

            if (userExpenseSum > limit.getLimitAmount()) {
                usersExceededLimit.add(user);
            }
        }

        return usersExceededLimit;
    }

    @Override
    public Map<String, Double> getExpensesGroupedByCategory() {
        return expenseRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    @Override
    public FinancialReportResponse getSystemFinancialReport() {
        // Total income
        Double totalIncome = incomeRepository.findAll()
                .stream()
                .mapToDouble(Income::getAmount)
                .sum();

        // Total expense
        Double totalExpense = expenseRepository.findAll()
                .stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        // Remaining balance
        Double balance = totalIncome - totalExpense;

        // Expense grouped by category
        Map<String, Double> expenseByCategory = getExpensesGroupedByCategory();

        return new FinancialReportResponse(totalIncome, totalExpense, balance, expenseByCategory);
    }

    @Override
    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAll()
                .stream()
                .filter(expense -> "USER".equalsIgnoreCase(String.valueOf(expense.getUser().getRole())))
                .map(expense -> {
                    User user = expense.getUser();
                    return new ExpenseResponse(
                            expense.getId(),
                            expense.getDescription(),
                            expense.getAmount(),
                            expense.getDate(),
                            expense.getCategory(),
                            user.getId(),
                            user.getUsername()
                    );
                })
                .collect(Collectors.toList());
    }



    @Override
    public List<IncomeWithUserLimitDTO> getAllIncomes() {
        List<Income> incomes = incomeRepository.findAll();

        return incomes.stream().map(income -> {
            User user = income.getUser();
            Double monthlyLimit = expenseLimitRepository.findByUser(user)
                    .map(Limit::getLimitAmount)
                    .orElse(null);

            return new IncomeWithUserLimitDTO(
                    income.getId(),
                    income.getAmount(),
                    income.getSource(),
                    income.getDate(),
                    user.getId(),
                    user.getUsername(),
                    monthlyLimit
            );
        }).collect(Collectors.toList());
    }




    @Override
    public ExpenseLimitResponse setExpenseLimit(Long userId, Double monthlyLimit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Limit limit = expenseLimitRepository.findByUser(user)
                .orElse(new Limit());

        limit.setUser(user);
        limit.setLimitAmount(monthlyLimit);
        expenseLimitRepository.save(limit);

        return new ExpenseLimitResponse(monthlyLimit);
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
