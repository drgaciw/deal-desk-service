package com.aciworldwide.dealdesk.rules.fact;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.tcv.PricingModel;
import org.jeasy.rules.api.Facts;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DealFactProviderTest {

    private final DealFactProvider dealFactProvider = new DealFactProvider();

    @Test
    void provideFacts_ShouldExtractAveragePayment_WhenPricingModelExists() {
        // Arrange
        BigDecimal expectedAveragePayment = new BigDecimal("123.45");
        PricingModel pricingModel = new PricingModel();
        pricingModel.setAveragePayment(expectedAveragePayment);

        Deal deal = Deal.builder()
                .pricingModel(pricingModel)
                .build();

        Facts facts = new Facts();

        // Act
        dealFactProvider.provideFacts(deal, facts);

        // Assert
        assertThat((BigDecimal) facts.get("averagePayment")).isEqualTo(expectedAveragePayment);
    }

    @Test
    void provideFacts_ShouldSetAveragePaymentToZero_WhenPricingModelExistsButAveragePaymentIsNull() {
        // Arrange
        PricingModel pricingModel = new PricingModel();
        pricingModel.setAveragePayment(null);

        Deal deal = Deal.builder()
                .pricingModel(pricingModel)
                .build();

        Facts facts = new Facts();

        // Act
        dealFactProvider.provideFacts(deal, facts);

        // Assert
        assertThat((BigDecimal) facts.get("averagePayment")).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void provideFacts_ShouldThrowException_WhenContextIsNotDeal() {
        Facts facts = new Facts();
        assertThrows(IllegalArgumentException.class, () -> dealFactProvider.provideFacts(new Object(), facts));
    }
}
