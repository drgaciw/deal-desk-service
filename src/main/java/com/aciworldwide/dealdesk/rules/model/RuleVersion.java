package com.aciworldwide.dealdesk.rules.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rule_versions")
public class RuleVersion {
    @Id
    private String id;
    private String ruleId;
    private int version;
    private String modifiedBy;
    private LocalDateTime modifiedAt;
    private String changeDescription;
    private DealRule ruleData;
} 