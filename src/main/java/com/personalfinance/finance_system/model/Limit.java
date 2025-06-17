package com.personalfinance.finance_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "expense_limit")
public class Limit {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double limitAmount;  // renamed to match service code

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Constructors
    public Limit() {}

    public Limit(Double limitAmount, User user) {
        this.limitAmount = limitAmount;
        this.user = user;
    }

    @Override
    public String toString() {
        return "Limit{" +
                "id=" + id +
                ", limitAmount=" + limitAmount +
                ", user=" + user +
                '}';
    }
}
