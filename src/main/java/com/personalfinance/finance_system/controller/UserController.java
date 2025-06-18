package com.personalfinance.finance_system.controller;

import com.personalfinance.finance_system.dto.*;
import com.personalfinance.finance_system.model.Expense;
import com.personalfinance.finance_system.model.Income;
import com.personalfinance.finance_system.model.Limit;
import com.personalfinance.finance_system.security.JwtService;
import com.personalfinance.finance_system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Dashboard Overview
    @GetMapping("/overview")
    public ResponseEntity<?> getUserOverview(Authentication authentication) {
        return ResponseEntity.ok(userService.getOverview(authentication.getName()));
    }

    // Chart data for visualizing expenses
    @GetMapping("/chart")
    public ResponseEntity<?> getUserChart(Authentication authentication) {
        return ResponseEntity.ok(userService.getChartData(authentication.getName()));
    }

    // Set monthly spending limit
    @PostMapping("/limit")
    public ResponseEntity<Limit> setLimit(@RequestBody ExpenseLimitRequest request, Authentication authentication) {
        return ResponseEntity.ok(userService.setLimit(authentication.getName(), request));
    }

    // Get current limit
    @GetMapping("/limit")
    public ResponseEntity<ExpenseLimitResponse> getLimit(Authentication authentication) {
        return ResponseEntity.ok(userService.getLimit(authentication.getName()));
    }

    // Add an expense
    @PostMapping("/expense")
    public ResponseEntity<Expense> addExpense(@RequestBody ExpenseRequest request, Authentication authentication) {
        return ResponseEntity.ok(userService.addExpense(authentication.getName(), request));
    }

    // Get all expenses for the user
    @GetMapping("/expenses")
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(Authentication authentication) {
        return ResponseEntity.ok(userService.getAllExpenses(authentication.getName()));
    }

    // Add income
    @PostMapping("/income")
    public ResponseEntity<Income> addIncome(@RequestBody IncomeRequest request, Authentication authentication) {
        return ResponseEntity.ok(userService.addIncome(authentication.getName(), request));
    }

    @GetMapping("/income")
    public ResponseEntity<List<IncomeResponseDTO>> getIncome(Authentication authentication) {
        return ResponseEntity.ok(userService.getIncome(authentication.getName()));
    }

    @PutMapping("/income/{id}")
    public ResponseEntity<IncomeResponseDTO> updateIncome(
            @PathVariable Long id,
            @RequestBody IncomeRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(userService.updateIncome(authentication.getName(), id, request));
    }

    @DeleteMapping("/income/{id}")
    public ResponseEntity<String> deleteIncome(
            @PathVariable("id") Long id,
            Authentication authentication) {
        String username = authentication.getName();
        userService.deleteIncome(username, id);
        return ResponseEntity.ok("Income deleted successfully");
    }


    // Get report summary
    @GetMapping("/report")
    public ResponseEntity<FinancialReportResponse> getFinancialReport(Authentication authentication) {
        return ResponseEntity.ok(userService.getFinancialReport(authentication.getName()));
    }
}
