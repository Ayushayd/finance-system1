package com.personalfinance.finance_system.controller;

import com.personalfinance.finance_system.dto.PromptRequest;
import com.personalfinance.finance_system.service.impl.GeminiService;
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
    public ResponseEntity<String> getInsight(@RequestBody PromptRequest request) {
        String result = geminiService.generateInsight(request);
        return ResponseEntity.ok(result);
    }
}
