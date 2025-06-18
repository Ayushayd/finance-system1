package com.personalfinance.finance_system.controller;

import com.personalfinance.finance_system.dto.*;
import com.personalfinance.finance_system.model.Expense;
import com.personalfinance.finance_system.model.Income;
import com.personalfinance.finance_system.model.Limit;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Admin Dashboard Overview: Total expenses, limits, users etc.
    @GetMapping("/overview")
    public ResponseEntity<?> getAdminOverview() {
        return ResponseEntity.ok(adminService.getOverview());
    }

    // Get all users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // Get expenses of a particular.
    @GetMapping("/users/{userId}/expenses")
    public ResponseEntity<List<Expense>> getUserExpenses(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserExpenses(userId));
    }

    // Get users who exceeded their monthly limits
    @GetMapping("/limit-exceeded")
    public ResponseEntity<List<User>> getUsersExceededLimit() {
        return ResponseEntity.ok(adminService.getUsersExceededLimit());
    }

    // Get expenses grouped by category across all users
    @GetMapping("/expenses-by-category")
    public ResponseEntity<Map<String, Double>> getExpensesByCategory() {
        return ResponseEntity.ok(adminService.getExpensesGroupedByCategory());
    }

    @GetMapping("/expenses")
    public List<ExpenseResponse> getAllExpenses() {
        return adminService.getAllExpenses();
    }


    @GetMapping("/incomes")
    public ResponseEntity<List<IncomeWithUserLimitDTO>> getAllIncomesForAdmin() {
        List<IncomeWithUserLimitDTO> result = adminService.getAllIncomes();
        return ResponseEntity.ok(result);
    }


    @PostMapping("/limit/{userId}")
    public ResponseEntity<ExpenseLimitResponse> setUserLimit(
            @PathVariable Long userId,
            @RequestBody ExpenseLimitRequest limit) {
        return ResponseEntity.ok(adminService.setExpenseLimit(userId, limit.getMonthlyLimit()));
    }


    // Generate a system-wide financial report
    @GetMapping("/report")
    public ResponseEntity<FinancialReportResponse> getSystemReport() {
        return ResponseEntity.ok(adminService.getSystemFinancialReport());
    }
}
