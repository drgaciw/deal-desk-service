package com.aciworldwide.dealdesk.mapper;

import com.aciworldwide.dealdesk.dto.DealRequestDTO;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealMapperHelperTest {

    @Mock
    private CurrencyService currencyService;

    private DealMapperHelper dealMapperHelper;

    @BeforeEach
    void setUp() {
        dealMapperHelper = new DealMapperHelper(currencyService);
    }

    @Test
    void convertCurrency_ValidCurrency_Converts() {
        when(currencyService.getConversionRate("EUR")).thenReturn(new BigDecimal("1.2"));

        BigDecimal result = dealMapperHelper.convertCurrency(new BigDecimal("100"), "EUR");

        assertThat(result).isEqualByComparingTo(new BigDecimal("120"));
    }

    @Test
    void convertCurrency_NullAmount_ReturnsZero() {
        BigDecimal result = dealMapperHelper.convertCurrency(null, "EUR");
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void resolveValue_Converts() {
        when(currencyService.getConversionRate("GBP")).thenReturn(new BigDecimal("1.5"));

        DealRequestDTO dto = new DealRequestDTO();
        dto.setValue(new BigDecimal("200"));
        dto.setCurrency("GBP");

        BigDecimal result = dealMapperHelper.resolveValue(dto);

        assertThat(result).isEqualByComparingTo(new BigDecimal("300"));
    }

    @Test
    void calculateDaysInStatus_ReturnsDays() {
        Deal deal = new Deal();
        deal.setStatusChangedAt(ZonedDateTime.now().minusDays(5));

        int days = dealMapperHelper.calculateDaysInStatus(deal);

        assertThat(days).isEqualTo(5);
    }

    @Test
    void calculateDaysInStatus_NullDate_ReturnsZero() {
        Deal deal = new Deal();
        deal.setStatusChangedAt(null);

        int days = dealMapperHelper.calculateDaysInStatus(deal);

        assertThat(days).isEqualTo(0);
    }

    @Test
    void determineNextAction_Draft_ReturnsSubmit() {
        String action = dealMapperHelper.determineNextAction(DealStatus.DRAFT);
        assertThat(action).isEqualTo("Submit for Approval");
    }

    @Test
    void determineNextAction_Null_ReturnsNoAction() {
        String action = dealMapperHelper.determineNextAction(null);
        assertThat(action).isEqualTo("No Action");
    }
}
