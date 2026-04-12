package com.aciworldwide.dealdesk.model.tcv;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Document(collection = "tcv_components")
@Schema(description = "A single component contributing to the Total Contract Value")
public class TCVComponent {
    @Id
    @Schema(description = "Unique component identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;
    
    @NotBlank(message = "Component name is required")
    @Schema(description = "Name of the TCV component", example = "Annual Support", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Optional description of the component")
    private String description;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Number of units", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantity;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    @Schema(description = "Price per unit", example = "5000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal unitPrice;

    @Schema(description = "ISO 4217 currency code for this component", example = "USD")
    private String currencyCode;

    @NotNull(message = "Component type is required")
    @Schema(description = "Type classification of the component", requiredMode = Schema.RequiredMode.REQUIRED)
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