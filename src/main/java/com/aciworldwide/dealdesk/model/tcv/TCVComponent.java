package com.aciworldwide.dealdesk.model.tcv;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "tcv_components")
public class TCVComponent {
    @Id
    private String id;
    
    private String name;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private String currencyCode;
    private ComponentType type;
    
    public enum ComponentType {
        PRODUCT,
        SERVICE,
        SUBSCRIPTION,
        IMPLEMENTATION,
        MAINTENANCE,
        SUPPORT
    }
    
    public BigDecimal calculateValue() {
        return quantity != null && unitPrice != null 
            ? quantity.multiply(unitPrice)
            : BigDecimal.ZERO;
    }
}