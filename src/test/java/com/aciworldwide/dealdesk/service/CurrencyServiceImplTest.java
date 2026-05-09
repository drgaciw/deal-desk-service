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
    void convertToUSD_ValidCurrency_ConvertsAmount() {
        assertThat(currencyService.convertToUSD(new BigDecimal("100.00"), "USD"))
                .isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(currencyService.convertToUSD(new BigDecimal("100.00"), "EUR"))
                .isEqualByComparingTo(new BigDecimal("110.0000"));
    }

    @Test
    void convertToUSD_InvalidCurrency_ReturnsOriginalAmount() {
        assertThat(currencyService.convertToUSD(new BigDecimal("100.00"), "UNKNOWN"))
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void convertToUSD_NullCurrency_ReturnsOriginalAmount() {
        assertThat(currencyService.convertToUSD(new BigDecimal("100.00"), null))
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void convertToUSD_EmptyCurrency_ReturnsOriginalAmount() {
        assertThat(currencyService.convertToUSD(new BigDecimal("100.00"), ""))
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
