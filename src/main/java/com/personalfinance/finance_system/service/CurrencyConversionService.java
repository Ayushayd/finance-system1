package com.personalfinance.finance_system.service;

public interface CurrencyConversionService {
    double convert(String fromCurrency, String toCurrency, double amount);
}
