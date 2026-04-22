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
    void getConversionRate_ValidCurrency_ReturnsRate() {
        assertThat(currencyService.getConversionRate("USD")).isEqualTo(BigDecimal.ONE);
        assertThat(currencyService.getConversionRate("EUR")).isNotNull().isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void getConversionRate_InvalidCurrency_ReturnsOne() {
        assertThat(currencyService.getConversionRate("UNKNOWN")).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void getConversionRate_NullCurrency_ReturnsOne() {
        assertThat(currencyService.getConversionRate(null)).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void getConversionRate_EmptyCurrency_ReturnsOne() {
        assertThat(currencyService.getConversionRate("")).isEqualTo(BigDecimal.ONE);
    }
}
