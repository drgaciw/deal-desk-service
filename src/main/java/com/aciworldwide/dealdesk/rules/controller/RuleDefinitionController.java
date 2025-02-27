package com.aciworldwide.dealdesk.rules.controller;

import com.aciworldwide.dealdesk.rules.model.RuleDefinition;
import com.aciworldwide.dealdesk.rules.service.RuleDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rules")
@Tag(name = "Rule Definitions", description = "Manage business rules and their definitions")
@RequiredArgsConstructor
public class RuleDefinitionController {

    private final RuleDefinitionService ruleDefinitionService;

    @PostMapping
    @Operation(summary = "Create a new rule definition")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rule created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid rule definition"),
        @ApiResponse(responseCode = "409", description = "Rule key already exists")
    })
    public ResponseEntity<RuleDefinition> createRule(
            @Valid @RequestBody RuleDefinition ruleDefinition) {
        return ResponseEntity.ok(ruleDefinitionService.createRule(ruleDefinition));
    }

    @PutMapping("/{ruleKey}")
    @Operation(summary = "Update an existing rule definition")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rule updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid rule definition"),
        @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<RuleDefinition> updateRule(
            @Parameter(description = "Rule identifier") @PathVariable String ruleKey,
            @Valid @RequestBody RuleDefinition ruleDefinition) {
        return ResponseEntity.ok(ruleDefinitionService.updateRule(ruleKey, ruleDefinition));
    }

    @DeleteMapping("/{ruleKey}")
    @Operation(summary = "Delete a rule definition")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rule deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<Void> deleteRule(
            @Parameter(description = "Rule identifier") @PathVariable String ruleKey) {
        ruleDefinitionService.deleteRule(ruleKey);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{ruleKey}")
    @Operation(summary = "Get a rule definition by key")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rule found"),
        @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<RuleDefinition> getRule(
            @Parameter(description = "Rule identifier") @PathVariable String ruleKey) {
        return ruleDefinitionService.getRule(ruleKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get all rules for a category")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<RuleDefinition>> getRulesByCategory(
            @Parameter(description = "Rule category") @PathVariable String category) {
        return ResponseEntity.ok(ruleDefinitionService.getRulesByCategory(category));
    }

    @GetMapping("/category/{category}/active")
    @Operation(summary = "Get active rules for a category")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<RuleDefinition>> getActiveRules(
            @Parameter(description = "Rule category") @PathVariable String category) {
        return ResponseEntity.ok(ruleDefinitionService.getActiveRules(category));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all active rule categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<String>> getActiveCategories() {
        return ResponseEntity.ok(ruleDefinitionService.getActiveCategories());
    }
}