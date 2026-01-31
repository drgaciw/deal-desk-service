package com.aciworldwide.dealdesk.service.impl;

import com.aciworldwide.dealdesk.service.CurrencyService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();

    static {
        // Base currency is USD (1.0)
        EXCHANGE_RATES.put("USD", BigDecimal.ONE);
        EXCHANGE_RATES.put("EUR", new BigDecimal("1.10")); // 1 EUR = 1.10 USD
        EXCHANGE_RATES.put("GBP", new BigDecimal("1.27")); // 1 GBP = 1.27 USD
        EXCHANGE_RATES.put("JPY", new BigDecimal("0.0068")); // 1 JPY = 0.0068 USD
        EXCHANGE_RATES.put("CAD", new BigDecimal("0.74")); // 1 CAD = 0.74 USD
        EXCHANGE_RATES.put("AUD", new BigDecimal("0.66")); // 1 AUD = 0.66 USD
    }

    @Override
    public BigDecimal getConversionRate(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return BigDecimal.ONE;
        }
        return EXCHANGE_RATES.getOrDefault(currencyCode.toUpperCase(), BigDecimal.ONE);
    }
}
