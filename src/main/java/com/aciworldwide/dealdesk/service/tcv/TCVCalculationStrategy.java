package com.aciworldwide.dealdesk.service.tcv;

import com.aciworldwide.dealdesk.model.tcv.TCVCalculation;
import com.aciworldwide.dealdesk.model.tcv.TCVComponent;
import com.aciworldwide.dealdesk.model.tcv.PricingModel;
import com.aciworldwide.dealdesk.exception.TCVValidationException;
import java.math.BigDecimal;
import java.util.List;

public interface TCVCalculationStrategy {
    BigDecimal calculateTCV(List<TCVComponent> components, PricingModel pricingModel);
    BigDecimal calculateNPV(TCVCalculation calculation, BigDecimal discountRate);
    void validateComponents(List<TCVComponent> components) throws TCVValidationException;
    
    default BigDecimal applyDiscounts(BigDecimal amount, BigDecimal discountRate) {
        if (discountRate.compareTo(BigDecimal.ZERO) < 0 || 
            discountRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Discount rate must be between 0 and 1");
        }
        return amount.multiply(BigDecimal.ONE.subtract(discountRate));
    }
}