package com.aciworldwide.dealdesk.service.impl;

import com.aciworldwide.dealdesk.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyServiceImplTest {

    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        currencyService = new CurrencyServiceImpl();
    }

    @Test
    void convert_ShouldReturnSameAmount_WhenCurrencyIsUSD() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal result = currencyService.convert(amount, "USD", "USD");
        assertThat(result).isEqualByComparingTo(amount);
    }

    @Test
    void convert_ShouldConvertEURToUSD() {
        BigDecimal amount = new BigDecimal("100.00");
        // 100 * 1.10 = 110.00
        BigDecimal expected = new BigDecimal("110.00");
        BigDecimal result = currencyService.convert(amount, "EUR", "USD");
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    void convert_ShouldReturnSameAmount_WhenCurrencyIsNull() {
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal result = currencyService.convert(amount, null, "USD");
        assertThat(result).isEqualByComparingTo(amount);
    }

    @Test
    void convert_ShouldReturnZero_WhenAmountIsNull() {
        BigDecimal result = currencyService.convert(null, "USD", "USD");
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void convert_ShouldThrowException_WhenTargetIsNotUSD() {
        BigDecimal amount = new BigDecimal("100.00");
        assertThrows(UnsupportedOperationException.class, () ->
            currencyService.convert(amount, "USD", "EUR")
        );
    }
}
