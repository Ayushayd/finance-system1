// com.personalfinance.finance_system.dto.IncomeWithUserLimitDTO.java
package com.personalfinance.finance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class IncomeWithUserLimitDTO {
    private Long id;
    private Double amount;
    private String source;
    private LocalDate date;
    private Long userId;
    private String username;
    private Double monthlyLimit;
}
