package com.aciworldwide.dealdesk.rules.controller;

import com.aciworldwide.dealdesk.rules.model.RuleDefinition;
import com.aciworldwide.dealdesk.rules.service.RuleDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rules")
@Tag(name = "Rule Definitions", description = "Manage business rules and their definitions")
@RequiredArgsConstructor
@Validated
public class RuleDefinitionController {

    private final RuleDefinitionService ruleDefinitionService;

    @PostMapping
    @Operation(summary = "Create a new rule definition")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rule created successfully",
                     content = @Content(schema = @Schema(implementation = RuleDefinition.class))),
        @ApiResponse(responseCode = "400", description = "Invalid rule definition", content = @Content),
        @ApiResponse(responseCode = "409", description = "Rule key already exists", content = @Content)
    })
    public ResponseEntity<RuleDefinition> createRule(
            @Valid @RequestBody RuleDefinition ruleDefinition) {
        return ResponseEntity.ok(ruleDefinitionService.createRule(ruleDefinition));
    }

    @PutMapping("/{ruleKey}")
    @Operation(summary = "Update an existing rule definition")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rule updated successfully",
                     content = @Content(schema = @Schema(implementation = RuleDefinition.class))),
        @ApiResponse(responseCode = "400", description = "Invalid rule definition", content = @Content),
        @ApiResponse(responseCode = "404", description = "Rule not found", content = @Content)
    })
    public ResponseEntity<RuleDefinition> updateRule(
            @Parameter(description = "Rule identifier") @PathVariable @NotBlank String ruleKey,
            @Valid @RequestBody RuleDefinition ruleDefinition) {
        return ResponseEntity.ok(ruleDefinitionService.updateRule(ruleKey, ruleDefinition));
    }

    @DeleteMapping("/{ruleKey}")
    @Operation(summary = "Delete a rule definition")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rule deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Rule not found", content = @Content)
    })
    public ResponseEntity<Void> deleteRule(
            @Parameter(description = "Rule identifier") @PathVariable @NotBlank String ruleKey) {
        ruleDefinitionService.deleteRule(ruleKey);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{ruleKey}")
    @Operation(summary = "Get a rule definition by key")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rule found",
                     content = @Content(schema = @Schema(implementation = RuleDefinition.class))),
        @ApiResponse(responseCode = "404", description = "Rule not found", content = @Content)
    })
    public ResponseEntity<RuleDefinition> getRule(
            @Parameter(description = "Rule identifier") @PathVariable @NotBlank String ruleKey) {
        return ruleDefinitionService.getRule(ruleKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all rule definitions with pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rules retrieved successfully",
                     content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<RuleDefinition>> getAllRules(
            @PageableDefault(size = 20, sort = "priority") Pageable pageable) {
        return ResponseEntity.ok(ruleDefinitionService.getAllRules(pageable));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get all rules for a category with pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rules retrieved successfully",
                     content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<RuleDefinition>> getRulesByCategory(
            @Parameter(description = "Rule category") @PathVariable @NotBlank String category,
            @PageableDefault(size = 20, sort = "priority") Pageable pageable) {
        return ResponseEntity.ok(ruleDefinitionService.getRulesByCategory(category, pageable));
    }

    @GetMapping("/category/{category}/active")
    @Operation(summary = "Get active rules for a category with pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Active rules retrieved successfully",
                     content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<RuleDefinition>> getActiveRules(
            @Parameter(description = "Rule category") @PathVariable @NotBlank String category,
            @PageableDefault(size = 20, sort = "priority") Pageable pageable) {
        return ResponseEntity.ok(ruleDefinitionService.getActiveRules(category, pageable));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all active rule categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    public ResponseEntity<List<String>> getActiveCategories() {
        return ResponseEntity.ok(ruleDefinitionService.getActiveCategories());
    }
}