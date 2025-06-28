package com.personalfinance.finance_system.service.impl;

import com.personalfinance.finance_system.dto.PromptRequest;
import com.personalfinance.finance_system.exception.ResourceNotFoundException;
import com.personalfinance.finance_system.model.*;
import com.personalfinance.finance_system.repository.ExpenseRepository;
import com.personalfinance.finance_system.repository.IncomeRepository;
import com.personalfinance.finance_system.repository.LimitRepository;
import com.personalfinance.finance_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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
        String enrichedPrompt = "ADMIN".equalsIgnoreCase(request.getRole())
                ? enrichPromptForAdmin(request.getPrompt())
                : enrichPromptForUser(request.getPrompt(), request.getUsername());

        return askGemini(enrichedPrompt);
    }

    private String enrichPromptForUser(String basePrompt, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Expense> expenses = expenseRepository.findByUser(user);
        List<Income> incomes = incomeRepository.findByUser(user);

        Optional<Limit> limitOpt = limitRepository.findByUser(user);

        StringBuilder sb = new StringBuilder(basePrompt)
                .append("\n\nUser: ").append(username).append("\n");

        if (expenses.isEmpty()) {
            sb.append("No expenses recorded for this month.\n");
        } else {
            sb.append("Expenses:\n");
            for (Expense e : expenses) {
                sb.append(String.format("- Date: %s, Category: %s, Amount: ₹%.2f, Description: %s%n",
                        e.getDate(), e.getCategory(), e.getAmount(), e.getDescription()));
            }
        }

        if (!incomes.isEmpty()) {
            sb.append("\nIncome:\n");
            for (Income i : incomes) {
                sb.append(String.format("- Date: %s, Source: %s, Amount: ₹%.2f%n",
                        i.getDate(), i.getSource(), i.getAmount()));
            }
        }

        limitOpt.ifPresent(limit -> sb.append("\nMonthly Expense Limit: ₹").append(limit.getLimitAmount()).append("\n"));

        return sb.toString();
    }

    private String enrichPromptForAdmin(String basePrompt) {
        StringBuilder sb = new StringBuilder(basePrompt).append("\n\n");

        List<User> users = userRepository.findAll();

        for (User user : users) {
            // 🔽 Skip if the user is an admin
            if (user.getRole() != Role.USER) {
                continue;
            }

            sb.append("\nUser: ").append(user.getUsername()).append("\n");

            List<Expense> expenses = expenseRepository.findByUser(user);
            List<Income> incomes = incomeRepository.findByUser(user);

            Optional<Limit> limitOpt = limitRepository.findByUser(user);

            if (expenses.isEmpty()) {
                sb.append(" - No expenses recorded.\n");
            } else {
                sb.append(" - Expenses:\n");
                for (Expense e : expenses) {
                    sb.append(String.format("   - Date: %s, Category: %s, Amount: ₹%.2f, Desc: %s%n",
                            e.getDate(), e.getCategory(), e.getAmount(), e.getDescription()));
                }
            }

            if (!incomes.isEmpty()) {
                sb.append(" - Income:\n");
                for (Income i : incomes) {
                    sb.append(String.format("   - Date: %s, Source: %s, Amount: ₹%.2f%n",
                            i.getDate(), i.getSource(), i.getAmount()));
                }
            }

            limitOpt.ifPresent(limit -> sb.append(" - Limit: ₹").append(limit.getLimitAmount()).append("\n"));
        }

        return sb.toString();
    }


    public String askGemini(String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

            Map<String, Object> message = Map.of("parts", List.of(Map.of("text", prompt)));
            Map<String, Object> requestBody = Map.of("contents", List.of(message));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return parts.get(0).get("text");
                    }
                }
            }

            return "Sorry, I couldn’t generate a response. Please try again.";
        } catch (RestClientException | ClassCastException e) {
            e.printStackTrace();
            return "An error occurred while communicating with the Gemini API.";
        }
    }
}
