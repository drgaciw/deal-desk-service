package com.aciworldwide.dealdesk.service.impl;

import com.aciworldwide.dealdesk.service.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CurrencyServiceImpl implements CurrencyService {

    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();

    static {
        // Mock rates to USD
        EXCHANGE_RATES.put("USD", BigDecimal.ONE);
        EXCHANGE_RATES.put("EUR", new BigDecimal("1.10"));
        EXCHANGE_RATES.put("GBP", new BigDecimal("1.27"));
        EXCHANGE_RATES.put("JPY", new BigDecimal("0.0068"));
        EXCHANGE_RATES.put("AUD", new BigDecimal("0.66"));
        EXCHANGE_RATES.put("CAD", new BigDecimal("0.74"));
    }

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (fromCurrency == null || fromCurrency.isEmpty()) {
            log.warn("Currency code is missing, assuming USD");
            return amount;
        }

        // Simulating external service call
        log.debug("Converting {} from {} to {}", amount, fromCurrency, toCurrency);

        // For this mock, we convert everything to USD first then to target if needed
        // But simply supporting X to USD is enough for the current requirement (default system currency is likely USD)

        if ("USD".equalsIgnoreCase(toCurrency)) {
            return convertToUSD(amount, fromCurrency);
        }

        throw new UnsupportedOperationException("Conversion to " + toCurrency + " is not supported in this mock implementation yet.");
    }

    @Override
    public BigDecimal convertToUSD(BigDecimal amount, String fromCurrency) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        String currency = fromCurrency != null ? fromCurrency.toUpperCase() : "USD";
        BigDecimal rate = EXCHANGE_RATES.getOrDefault(currency, BigDecimal.ONE);

        if (!EXCHANGE_RATES.containsKey(currency)) {
            log.warn("Exchange rate not found for {}, using 1.0", currency);
        }

        return amount.multiply(rate);
    }
}
