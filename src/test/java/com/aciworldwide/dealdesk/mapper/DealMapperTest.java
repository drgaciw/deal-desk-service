package com.aciworldwide.dealdesk.mapper;

import com.aciworldwide.dealdesk.dto.DealRequestDTO;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.service.impl.CurrencyServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DealMapperTest.TestConfig.class)
class DealMapperTest {

    @Configuration
    @ComponentScan(basePackages = "com.aciworldwide.dealdesk.mapper")
    @Import({CurrencyServiceImpl.class})
    static class TestConfig {
    }

    @Autowired
    private DealMapper dealMapper;

    @Test
    void toDeal_ShouldConvertCurrency() {
        DealRequestDTO dto = new DealRequestDTO();
        dto.setValue(new BigDecimal("100.00"));
        dto.setCurrency("EUR");

        Deal deal = dealMapper.toDeal(dto);

        // 100 * 1.10 = 110.00
        assertThat(deal.getValue()).isEqualByComparingTo(new BigDecimal("110.00"));
    }

    @Test
    void toDeal_ShouldDefaultToUSD_WhenCurrencyMissing() {
        DealRequestDTO dto = new DealRequestDTO();
        dto.setValue(new BigDecimal("100.00"));
        dto.setCurrency(null);

        Deal deal = dealMapper.toDeal(dto);

        assertThat(deal.getValue()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
