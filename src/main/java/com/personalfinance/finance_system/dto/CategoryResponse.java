package com.personalfinance.finance_system.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CategoryResponse {

    // Getter and Setter
    private String name;

    public CategoryResponse(String name) {
        this.name = name;
    }

}
