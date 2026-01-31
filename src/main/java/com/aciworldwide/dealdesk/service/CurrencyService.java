package com.aciworldwide.dealdesk.service;

import java.math.BigDecimal;

public interface CurrencyService {
    BigDecimal getConversionRate(String currencyCode);
}
