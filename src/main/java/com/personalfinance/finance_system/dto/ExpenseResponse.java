package com.personalfinance.finance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponse {

    // Getters and Setters
    private Long id;
    private String description;
    private Double amount;
    private LocalDate date;
    private String category;
    private Long userId;
    private String username;

}
