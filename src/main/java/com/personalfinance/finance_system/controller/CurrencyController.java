package com.personalfinance.finance_system.controller;

import com.personalfinance.finance_system.service.CurrencyConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/currency")
@CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET})
public class CurrencyController {

    private final CurrencyConversionService conversionService;

    public CurrencyController(CurrencyConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @GetMapping("/convert")
    public ResponseEntity<Double> convert(@RequestParam String from, @RequestParam String to, @RequestParam double amount) {
        double convertedAmount = conversionService.convert(from, to, amount);
        return ResponseEntity.ok(convertedAmount);
    }

}
