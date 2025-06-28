package com.personalfinance.finance_system.service.impl;

import com.personalfinance.finance_system.dto.PromptRequest;
import com.personalfinance.finance_system.exception.ResourceNotFoundException;
import com.personalfinance.finance_system.model.Expense;
import com.personalfinance.finance_system.model.Income;
import com.personalfinance.finance_system.model.Limit;
import com.personalfinance.finance_system.model.User;
import com.personalfinance.finance_system.repository.ExpenseRepository;
import com.personalfinance.finance_system.repository.IncomeRepository;
import com.personalfinance.finance_system.repository.LimitRepository;
import com.personalfinance.finance_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final LimitRepository limitRepository;

    public GeminiService(UserRepository userRepository,
                         ExpenseRepository expenseRepository,
                         IncomeRepository incomeRepository,
                         LimitRepository limitRepository) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.limitRepository = limitRepository;
    }

    public String generateInsight(PromptRequest request) {
        String enrichedPrompt = enrichPrompt(request.getPrompt(), request.getUsername());
        return askGemini(enrichedPrompt);
    }

    private String enrichPrompt(String basePrompt, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findByUserAndDateBetween(user, start, end);
        List<Income> incomes = incomeRepository.findByUserAndDateBetween(user, start, end);
        Optional<Limit> limitOpt = limitRepository.findByUser(user);

        StringBuilder sb = new StringBuilder(basePrompt).append("\n\n");

        if (expenses.isEmpty()) {
            sb.append("No expenses were recorded for this month.\n");
        } else {
            sb.append("Expenses:\n");
            for (Expense e : expenses) {
                sb.append(String.format("- Date: %s, Category: %s, Amount: ₹%.2f, Description: %s\n",
                        e.getDate(), e.getCategory(), e.getAmount(), e.getDescription()));
            }
        }

        if (!incomes.isEmpty()) {
            sb.append("\nIncome:\n");
            for (Income i : incomes) {
                sb.append(String.format("- Date: %s, Source: %s, Amount: ₹%.2f\n",
                        i.getDate(), i.getSource(), i.getAmount()));
            }
        }

        limitOpt.ifPresent(limit -> sb.append("\nMonthly Expense Limit: ₹").append(limit.getLimitAmount()).append("\n"));

        return sb.toString();
    }

    public String askGemini(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");

        return parts.get(0).get("text");
    }
}
