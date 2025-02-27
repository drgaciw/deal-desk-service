package com.aciworldwide.dealdesk.rules.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "rule_definitions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleDefinition {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("rule_key")
    private String ruleKey;

    private String name;
    private String description;
    private String category;
    private Integer priority;

    @Field("condition_expression")
    private String conditionExpression;

    @Field("action_expression")
    private String actionExpression;

    private boolean enabled;

    private Map<String, String> parameters;

    @Field("valid_from")
    private LocalDateTime validFrom;

    @Field("valid_to")
    private LocalDateTime validTo;

    @Version
    private Long version;

    @Field("created_by")
    private String createdBy;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("last_modified_by")
    private String lastModifiedBy;

    @Field("last_modified_at")
    private LocalDateTime lastModifiedAt;
}