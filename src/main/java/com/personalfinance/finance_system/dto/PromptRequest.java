package com.personalfinance.finance_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromptRequest {

    private String username;
    private String role;

    @NotBlank(message = "Prompt cannot be empty")
    private String prompt;
}
