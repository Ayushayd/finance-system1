package com.personalfinance.finance_system.controller;

import com.personalfinance.finance_system.dto.ExpenseRequest;
import com.personalfinance.finance_system.dto.ExpenseResponse;
import com.personalfinance.finance_system.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(@RequestBody ExpenseRequest request, Authentication authentication) {
        return ResponseEntity.ok(expenseService.addExpense(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getUserExpenses(Authentication authentication) {
        return ResponseEntity.ok(expenseService.getUserExpenses(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpense(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(expenseService.getExpenseById(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable Long id, @RequestBody ExpenseRequest request, Authentication authentication) {
        return ResponseEntity.ok(expenseService.updateExpense(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id, Authentication authentication) {
        expenseService.deleteExpense(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}