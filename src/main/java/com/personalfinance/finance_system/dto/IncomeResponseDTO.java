package com.personalfinance.finance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomeResponseDTO {
    private Long id;
    private Double amount;
    private String source;
    private LocalDate date;
    private Long userId;
    private String username;
}
