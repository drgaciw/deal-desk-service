package com.aciworldwide.dealdesk.service;

import java.math.BigDecimal;

public interface CurrencyService {
    BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency);

    // Convenience method assuming target is USD (system default)
    BigDecimal convertToUSD(BigDecimal amount, String fromCurrency);
}
