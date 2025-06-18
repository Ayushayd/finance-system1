package com.personalfinance.finance_system.service.impl;

import com.personalfinance.finance_system.service.CurrencyConversionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

    @Value("${currency.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public double convert(String fromCurrency, String toCurrency, double amount) {
        String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/pair/" +
                fromCurrency + "/" + toCurrency;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response != null && "success".equals(response.get("result"))) {
            double rate = (double) response.get("conversion_rate");
            return amount * rate;
        } else {
            throw new RuntimeException("Failed to fetch conversion rate");
        }
    }
}
