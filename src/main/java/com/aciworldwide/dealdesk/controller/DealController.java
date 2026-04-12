package com.aciworldwide.dealdesk.controller;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aciworldwide.dealdesk.dto.DealRequestDTO;
import com.aciworldwide.dealdesk.dto.DealResponseDTO;
import com.aciworldwide.dealdesk.mapper.DealMapper;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.service.DealService;

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
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/deals")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Deal Management", description = "APIs for managing deals")
public class DealController {

    private final DealService dealService;
    private final DealMapper dealMapper;

    @PostMapping
    @Operation(summary = "Create a new deal",
               description = "Creates a deal record and synchronises it with the linked Salesforce Opportunity.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Deal created successfully",
                     content = @Content(schema = @Schema(implementation = DealResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
        @ApiResponse(responseCode = "409", description = "Deal with same Salesforce Opportunity ID already exists", content = @Content)
    })
    @PreAuthorize("hasRole('DEAL_CREATOR')")
    public ResponseEntity<DealResponseDTO> createDeal(@Valid @RequestBody DealRequestDTO requestDTO) {
        log.debug("Creating new deal: {}", requestDTO);
        Deal deal = dealMapper.toDeal(requestDTO);
        Deal createdDeal = dealService.createDeal(deal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dealMapper.toResponseDTO(createdDeal));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get deal by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deal found",
                     content = @Content(schema = @Schema(implementation = DealResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Deal not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('DEAL_VIEWER', 'DEAL_CREATOR', 'DEAL_APPROVER')")
    public ResponseEntity<DealResponseDTO> getDeal(
            @Parameter(description = "Deal identifier") @PathVariable @NotBlank String id) {
        log.debug("Fetching deal with ID: {}", id);
        Deal deal = dealService.getDealById(id);
        return ResponseEntity.ok(dealMapper.toResponseDTO(deal));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing deal")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deal updated successfully",
                     content = @Content(schema = @Schema(implementation = DealResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
        @ApiResponse(responseCode = "404", description = "Deal not found", content = @Content)
    })
    @PreAuthorize("hasRole('DEAL_CREATOR')")
    public ResponseEntity<DealResponseDTO> updateDeal(
            @Parameter(description = "Deal identifier") @PathVariable @NotBlank String id,
            @Valid @RequestBody DealRequestDTO requestDTO) {
        log.debug("Updating deal with ID: {}", id);
        Deal updatedDeal = dealService.updateDeal(id, dealMapper.toDeal(requestDTO));
        return ResponseEntity.ok(dealMapper.toResponseDTO(updatedDeal));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a deal")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deal deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Deal cannot be deleted in its current state", content = @Content),
        @ApiResponse(responseCode = "404", description = "Deal not found", content = @Content)
    })
    @PreAuthorize("hasRole('DEAL_ADMIN')")
    public ResponseEntity<Void> deleteDeal(
            @Parameter(description = "Deal identifier") @PathVariable @NotBlank String id) {
        log.debug("Deleting deal with ID: {}", id);
        dealService.deleteDeal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Search deals with filters",
               description = "Returns a paginated list of deals. Supply at most one of: status, accountId, owner, minValue, or a date range (since / until).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deals retrieved successfully",
                     content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters", content = @Content)
    })
    @PreAuthorize("hasAnyRole('DEAL_VIEWER', 'DEAL_CREATOR', 'DEAL_APPROVER')")
    public ResponseEntity<Page<DealResponseDTO>> searchDeals(
            @Parameter(description = "Filter by deal status")
            @RequestParam(required = false) DealStatus status,
            @Parameter(description = "Filter by Salesforce account ID")
            @RequestParam(required = false) String accountId,
            @Parameter(description = "Filter by sales representative / owner ID")
            @RequestParam(required = false) String owner,
            @Parameter(description = "Minimum deal value (only applied together with status)")
            @RequestParam(required = false) BigDecimal minValue,
            @Parameter(description = "Include deals created on or after this date (ISO-8601 with offset, e.g. 2025-01-01T00:00:00+00:00)")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime since,
            @Parameter(description = "Include deals created on or before this date (ISO-8601 with offset)")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime until,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Deal> page = dealService.searchDeals(status, accountId, owner, minValue, since, until, pageable);
        return ResponseEntity.ok(page.map(dealMapper::toResponseDTO));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit deal for approval")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deal submitted successfully",
                     content = @Content(schema = @Schema(implementation = DealResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Deal cannot be submitted in its current state", content = @Content),
        @ApiResponse(responseCode = "404", description = "Deal not found", content = @Content)
    })
    @PreAuthorize("hasRole('DEAL_CREATOR')")
    public ResponseEntity<DealResponseDTO> submitForApproval(
            @Parameter(description = "Deal identifier") @PathVariable @NotBlank String id) {
        log.debug("Submitting deal for approval: {}", id);
        Deal deal = dealService.submitForApproval(id);
        return ResponseEntity.ok(dealMapper.toResponseDTO(deal));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a deal")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deal approved successfully",
                     content = @Content(schema = @Schema(implementation = DealResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Deal cannot be approved in its current state", content = @Content),
        @ApiResponse(responseCode = "404", description = "Deal not found", content = @Content)
    })
    @PreAuthorize("hasRole('DEAL_APPROVER')")
    public ResponseEntity<DealResponseDTO> approveDeal(
            @Parameter(description = "Deal identifier") @PathVariable @NotBlank String id,
            @Parameter(description = "Approver user ID", required = true)
            @RequestHeader("X-User-ID") @NotBlank String approverUserId) {
        log.debug("Approving deal: {} by user: {}", id, approverUserId);
        Deal deal = dealService.approveDeal(id, approverUserId);
        return ResponseEntity.ok(dealMapper.toResponseDTO(deal));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a deal")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deal rejected successfully",
                     content = @Content(schema = @Schema(implementation = DealResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Rejection reason is required", content = @Content),
        @ApiResponse(responseCode = "404", description = "Deal not found", content = @Content)
    })
    @PreAuthorize("hasRole('DEAL_APPROVER')")
    public ResponseEntity<DealResponseDTO> rejectDeal(
            @Parameter(description = "Deal identifier") @PathVariable @NotBlank String id,
            @Parameter(description = "Rejector user ID", required = true)
            @RequestHeader("X-User-ID") @NotBlank String rejectorUserId,
            @Parameter(description = "Reason for rejection", required = true)
            @RequestParam @NotBlank String reason) {
        log.debug("Rejecting deal: {} by user: {}", id, rejectorUserId);
        Deal deal = dealService.rejectDeal(id, rejectorUserId, reason);
        return ResponseEntity.ok(dealMapper.toResponseDTO(deal));
    }

    @PostMapping("/{id}/sync")
    @Operation(summary = "Sync deal with Salesforce")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deal synced successfully",
                     content = @Content(schema = @Schema(implementation = DealResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Deal not found", content = @Content),
        @ApiResponse(responseCode = "503", description = "Salesforce integration error", content = @Content)
    })
    @PreAuthorize("hasAnyRole('DEAL_CREATOR', 'DEAL_ADMIN')")
    public ResponseEntity<DealResponseDTO> syncWithSalesforce(
            @Parameter(description = "Deal identifier") @PathVariable @NotBlank String id) {
        log.debug("Syncing deal with Salesforce: {}", id);
        Deal deal = dealService.syncWithSalesforce(id);
        return ResponseEntity.ok(dealMapper.toResponseDTO(deal));
    }

    @PostMapping("/batch-sync")
    @Operation(summary = "Batch sync deals with Salesforce",
               description = "Asynchronously syncs multiple deals. Returns 202 Accepted immediately.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Batch sync accepted"),
        @ApiResponse(responseCode = "400", description = "Invalid list of deal IDs", content = @Content)
    })
    @PreAuthorize("hasRole('DEAL_ADMIN')")
    public ResponseEntity<Void> batchSyncWithSalesforce(@RequestBody List<String> ids) {
        log.debug("Batch syncing deals with Salesforce: {}", ids);
        dealService.batchSyncWithSalesforce(ids);
        return ResponseEntity.accepted().build();
    }
}