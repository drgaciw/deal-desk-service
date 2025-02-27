package com.aciworldwide.dealdesk.rules.model;

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
@Document(collection = "rules")
public class DealRule {
    @Id
    private String id;
    private String name;
    private String description;
    private boolean active;
    private boolean required;
    private int version;
    private String category;
    private int priority;
    private boolean cacheable;
} 