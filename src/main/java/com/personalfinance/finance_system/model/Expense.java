package com.personalfinance.finance_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
public class Expense {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private Double amount;
    private LocalDate date;

    private String description;

    @ManyToOne
    private User user;

    // Constructors
    public Expense() {}

    public Expense(String category, Double amount, LocalDate date, String description, User user) {
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.user = user;
    }

}

