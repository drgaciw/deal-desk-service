package com.aciworldwide.dealdesk.rules.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "rule_definitions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Business rule definition")
public class RuleDefinition {

    @Id
    @Schema(description = "Unique rule identifier", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Indexed(unique = true)
    @Field("rule_key")
    @NotBlank(message = "Rule key is required")
    @Size(max = 100, message = "Rule key must not exceed 100 characters")
    @Schema(description = "Unique rule key used to identify the rule", example = "VOLUME_DISCOUNT_RULE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ruleKey;

    @NotBlank(message = "Rule name is required")
    @Size(max = 200, message = "Rule name must not exceed 200 characters")
    @Schema(description = "Human-readable rule name", example = "Volume Discount Rule", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Optional rule description")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Schema(description = "Rule category for grouping", example = "PRICING", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    @NotNull(message = "Priority is required")
    @Min(value = 1, message = "Priority must be at least 1")
    @Schema(description = "Rule execution priority; higher values run first", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer priority;

    @Field("condition_expression")
    @NotBlank(message = "Condition expression is required")
    @Schema(description = "SpEL expression that must evaluate to true for the rule to fire", requiredMode = Schema.RequiredMode.REQUIRED)
    private String conditionExpression;

    @Field("action_expression")
    @NotBlank(message = "Action expression is required")
    @Schema(description = "SpEL expression executed when the condition is met", requiredMode = Schema.RequiredMode.REQUIRED)
    private String actionExpression;

    @Schema(description = "Whether the rule is currently active")
    private boolean enabled;

    @Schema(description = "Optional key-value parameters available to rule expressions")
    private Map<String, String> parameters;

    @Field("valid_from")
    @NotNull(message = "Valid from date is required")
    @Schema(description = "Date/time from which the rule is valid", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime validFrom;

    @Field("valid_to")
    @Schema(description = "Optional date/time after which the rule is no longer valid")
    private LocalDateTime validTo;

    @Version
    @Schema(description = "Optimistic-lock version", accessMode = Schema.AccessMode.READ_ONLY)
    private Long version;

    @Field("created_by")
    @Schema(description = "User who created the rule", accessMode = Schema.AccessMode.READ_ONLY)
    private String createdBy;

    @Field("created_at")
    @Schema(description = "Timestamp when the rule was created", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Field("last_modified_by")
    @Schema(description = "User who last modified the rule", accessMode = Schema.AccessMode.READ_ONLY)
    private String lastModifiedBy;

    @Field("last_modified_at")
    @Schema(description = "Timestamp when the rule was last modified", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastModifiedAt;
}