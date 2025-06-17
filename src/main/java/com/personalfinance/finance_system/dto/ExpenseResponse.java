package com.personalfinance.finance_system.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class ExpenseResponse {

    // Getters and Setters
    private Long id;
    private String description;
    private Double amount;
    private LocalDate date;
    private String category;

    public ExpenseResponse() {
    }

    public ExpenseResponse(Long id, String description, Double amount, LocalDate date, String category) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

}
