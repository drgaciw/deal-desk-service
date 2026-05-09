package com.aciworldwide.dealdesk.repository;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalValueResult {
    private String id;
    private BigDecimal total;
}
