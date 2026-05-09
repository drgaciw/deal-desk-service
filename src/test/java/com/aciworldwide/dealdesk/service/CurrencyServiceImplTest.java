package com.aciworldwide.dealdesk.service;

import com.aciworldwide.dealdesk.service.impl.CurrencyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceImplTest {

    private final CurrencyServiceImpl currencyService = new CurrencyServiceImpl();

    @Test
    void convertToUSD_ValidCurrency_UsesConfiguredRate() {
        assertThat(currencyService.convertToUSD(new BigDecimal("10"), "USD")).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(currencyService.convertToUSD(new BigDecimal("10"), "EUR")).isEqualByComparingTo(new BigDecimal("11.00"));
    }

    @Test
    void convertToUSD_InvalidCurrency_UsesOneToOneRate() {
        assertThat(currencyService.convertToUSD(new BigDecimal("10"), "UNKNOWN")).isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    void convertToUSD_NullCurrency_UsesUSDRate() {
        assertThat(currencyService.convertToUSD(new BigDecimal("10"), null)).isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    void convertToUSD_EmptyCurrency_UsesOneToOneRate() {
        assertThat(currencyService.convertToUSD(new BigDecimal("10"), "")).isEqualByComparingTo(new BigDecimal("10"));
    }
}
