package com.personalfinance.finance_system.controller;

import com.personalfinance.finance_system.dto.PromptRequest;
import com.personalfinance.finance_system.service.impl.GeminiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/insight")
    public ResponseEntity<String> getInsight(@Valid @RequestBody PromptRequest request) {
        try {
            String result = geminiService.generateInsight(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("AI Insight generation failed: " + e.getMessage());
        }
    }

}
