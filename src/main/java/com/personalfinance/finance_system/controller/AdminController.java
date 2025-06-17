package com.personalfinance.finance_system.controller;

import com.personalfinance.finance_system.model.Expense;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getAdminOverview() {
        return ResponseEntity.ok(adminService.getOverview());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{userId}/expenses")
    public ResponseEntity<List<Expense>> getUserExpenses(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserExpenses(userId));
    }

    @GetMapping("/limit-exceeded")
    public ResponseEntity<List<User>> getUsersExceededLimit() {
        return ResponseEntity.ok(adminService.getUsersExceededLimit());
    }
}