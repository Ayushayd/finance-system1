package com.personalfinance.finance_system.controller;

import com.personalfinance.finance_system.dto.ExpenseLimitRequest;
import com.personalfinance.finance_system.model.Limit;
import com.personalfinance.finance_system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getUserOverview(Authentication authentication) {
        return ResponseEntity.ok(userService.getOverview(authentication.getName()));
    }

    @GetMapping("/chart")
    public ResponseEntity<?> getUserChart(Authentication authentication) {
        return ResponseEntity.ok(userService.getChartData(authentication.getName()));
    }

    @PostMapping("/limit")
    public ResponseEntity<Limit> setLimit(@RequestBody ExpenseLimitRequest request, Authentication authentication) {
        return ResponseEntity.ok(userService.setLimit(authentication.getName(), request));
    }

    @GetMapping("/limit")
    public ResponseEntity<Limit> getLimit(Authentication authentication) {
        return ResponseEntity.ok(userService.getLimit(authentication.getName()));
    }
}