package com.aciworldwide.dealdesk.controller;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/deals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Deal Management", description = "APIs for managing deals")
public class DealController {

    private final DealService dealService;
    private final DealMapper dealMapper;

    @PostMapping
    @Operation(summary = "Create a new deal")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Deal created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Deal with same Salesforce Opportunity ID already exists")
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
        @ApiResponse(responseCode = "200", description = "Deal found"),
        @ApiResponse(responseCode = "404", description = "Deal not found")
    })
    @PreAuthorize("hasAnyRole('DEAL_VIEWER', 'DEAL_CREATOR', 'DEAL_APPROVER')")
    public ResponseEntity<DealResponseDTO> getDeal(@PathVariable String id) {
        log.debug("Fetching deal with ID: {}", id);
        Deal deal = dealService.getDealById(id);
        return ResponseEntity.ok(dealMapper.toResponseDTO(deal));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing deal")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deal updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Deal not found")
    })
    @PreAuthorize("hasRole('DEAL_CREATOR')")
    public ResponseEntity<DealResponseDTO> updateDeal(
            @PathVariable String id,
            @Valid @RequestBody DealRequestDTO requestDTO) {
        log.debug("Updating deal with ID: {}", id);
        Deal updatedDeal = dealService.updateDeal(id, dealMapper.toDeal(requestDTO));
        return ResponseEntity.ok(dealMapper.toResponseDTO(updatedDeal));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a deal")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deal deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Deal not found")
    })
    @PreAuthorize("hasRole('DEAL_ADMIN')")
    public ResponseEntity<Void> deleteDeal(@PathVariable String id) {
        log.debug("Deleting deal with ID: {}", id);
        dealService.deleteDeal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Search deals with filters")
    @PreAuthorize("hasAnyRole('DEAL_VIEWER', 'DEAL_CREATOR', 'DEAL_APPROVER')")
    public ResponseEntity<Collection<DealResponseDTO>> searchDeals(
            @Parameter(description = "Deal status filter")
            @RequestParam(required = false) DealStatus status,
            @Parameter(description = "Account ID filter")
            @RequestParam(required = false) String accountId,
            @Parameter(description = "Sales representative ID filter")
            @RequestParam(required = false) String salesRepId,
            @Parameter(description = "Minimum deal value")
            @RequestParam(required = false) BigDecimal minValue,
            @Parameter(description = "Search deals created since")
            @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") ZonedDateTime since,
            @Parameter(hidden = true) Pageable pageable) {
        
        Page<Deal> deals;
        if (status != null) {
            deals = dealService.getDealsByStatus(status, pageable);
        } else if (accountId != null) {
            deals = dealService.getDealsByAccount(accountId, pageable);
        } else if (salesRepId != null) {
            deals = dealService.getDealsBySalesRep(salesRepId, pageable);
        } else if (minValue != null) {
            deals = dealService.getHighValueDeals(minValue, DealStatus.APPROVED, pageable);
        } else if (since != null) {
            deals = dealService.getRecentDeals(since, List.of(DealStatus.values()), pageable);
        } else {
            deals = dealService.getAllDeals(pageable);
        }
        
        return ResponseEntity.ok(dealMapper.toResponseDTOList(deals.getContent()));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit deal for approval")
    @PreAuthorize("hasRole('DEAL_CREATOR')")
    public ResponseEntity<DealResponseDTO> submitForApproval(@PathVariable String id) {
        log.debug("Submitting deal for approval: {}", id);
        Deal deal = dealService.submitForApproval(id);
        return ResponseEntity.ok(dealMapper.toResponseDTO(deal));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a deal")
    @PreAuthorize("hasRole('DEAL_APPROVER')")
    public ResponseEntity<DealResponseDTO> approveDeal(
            @PathVariable String id,
            @RequestHeader("X-User-ID") String approverUserId) {
        log.debug("Approving deal: {} by user: {}", id, approverUserId);
        Deal deal = dealService.approveDeal(id, approverUserId);
        return ResponseEntity.ok(dealMapper.toResponseDTO(deal));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a deal")
    @PreAuthorize("hasRole('DEAL_APPROVER')")
    public ResponseEntity<DealResponseDTO> rejectDeal(
            @PathVariable String id,
            @RequestHeader("X-User-ID") String rejectorUserId,
            @RequestParam String reason) {
        log.debug("Rejecting deal: {} by user: {}", id, rejectorUserId);
        Deal deal = dealService.rejectDeal(id, rejectorUserId, reason);
        return ResponseEntity.ok(dealMapper.toResponseDTO(deal));
    }

    @PostMapping("/{id}/sync")
    @Operation(summary = "Sync deal with Salesforce")
    @PreAuthorize("hasAnyRole('DEAL_CREATOR', 'DEAL_ADMIN')")
    public ResponseEntity<DealResponseDTO> syncWithSalesforce(@PathVariable String id) {
        log.debug("Syncing deal with Salesforce: {}", id);
        Deal deal = dealService.syncWithSalesforce(id);
        return ResponseEntity.ok(dealMapper.toResponseDTO(deal));
    }

    @PostMapping("/batch-sync")
    @Operation(summary = "Batch sync deals with Salesforce")
    @PreAuthorize("hasRole('DEAL_ADMIN')")
    public ResponseEntity<Void> batchSyncWithSalesforce(@RequestBody List<String> ids) {
        log.debug("Batch syncing deals with Salesforce: {}", ids);
        dealService.batchSyncWithSalesforce(ids);
        return ResponseEntity.accepted().build();
    }
}