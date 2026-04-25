package com.aciworldwide.dealdesk.service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.aciworldwide.dealdesk.exception.DealNotFoundException;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;

public interface DealService {
    
    /**
     * Creates a new deal in the system.
     *
     * @param deal the deal object containing all required fields
     * @return the created deal with generated fields (e.g., ID, timestamps)
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    Deal createDeal(Deal deal);
    
    /**
     * Retrieves a deal by its ID.
     *
     * @param id the deal ID (must not be null or empty)
     * @return the deal with all associated data
     * @throws DealNotFoundException if the deal is not found
     * @throws IllegalArgumentException if the ID is invalid
     */
    Deal getDealById(String id);
    
    /**
     * Updates an existing deal with new data.
     *
     * @param id the ID of the deal to update (must not be null or empty)
     * @param deal the deal object containing updated fields
     * @return the updated deal
     * @throws DealNotFoundException if the deal is not found
     * @throws IllegalArgumentException if the ID is invalid or required fields are missing
     */
    Deal updateDeal(String id, Deal deal);
    
    /**
     * Deletes a deal from the system.
     *
     * @param id the ID of the deal to delete (must not be null or empty)
     * @throws DealNotFoundException if the deal is not found
     * @throws IllegalArgumentException if the ID is invalid
     * @throws IllegalStateException if the deal cannot be deleted due to its current state
     */
    void deleteDeal(String id);
    
    /**
     * Retrieves all deals in the system.
     *
     * @return list of all deals, ordered by creation date descending
     * @throws DataAccessException if there is an issue accessing the data store
     */
    List<Deal> getAllDeals();

    /**
     * Retrieves a page of deals in the system.
     *
     * @param pageable pagination information
     * @return page of deals
     * @throws DataAccessException if there is an issue accessing the data store
     */
    Page<Deal> getAllDeals(Pageable pageable);
    
    /**
     * Retrieves deals filtered by their status.
     *
     * @param status the deal status to filter by (must not be null)
     * @return list of deals matching the specified status
     * @throws IllegalArgumentException if status is null
     */
    List<Deal> getDealsByStatus(DealStatus status);
    
    /**
     * Retrieves deals associated with a specific account.
     *
     * @param accountId the account ID to filter by (must not be null or empty)
     * @return list of deals associated with the account
     * @throws IllegalArgumentException if accountId is invalid
     */
    List<Deal> getDealsByAccount(String accountId);
    
    /**
     * Retrieves deals managed by a specific sales representative.
     *
     * @param salesRepId the sales representative ID to filter by (must not be null or empty)
     * @return list of deals managed by the sales representative
     * @throws IllegalArgumentException if salesRepId is invalid
     */
    List<Deal> getDealsBySalesRep(String salesRepId);
    
    /**
     * Retrieves high-value deals that meet the minimum value threshold and status.
     *
     * @param minValue the minimum deal value threshold (must be positive)
     * @param status the deal status to filter by (must not be null)
     * @return list of high-value deals meeting the criteria
     * @throws IllegalArgumentException if minValue is not positive or status is null
     */
    List<Deal> getHighValueDeals(BigDecimal minValue, DealStatus status);
    
    /**
     * Retrieves deals created since a specific date, filtered by status.
     *
     * @param since the cutoff date for deal creation (must not be null)
     * @param statuses list of statuses to include (null or empty list returns all statuses)
     * @return list of recent deals meeting the criteria
     * @throws IllegalArgumentException if since is null
     */
    List<Deal> getRecentDeals(ZonedDateTime since, List<DealStatus> statuses);
    
    /**
     * Submits a deal for approval, transitioning it to the PENDING_APPROVAL state.
     *
     * @param id the ID of the deal to submit (must not be null or empty)
     * @return the updated deal with new status
     * @throws DealNotFoundException if the deal is not found
     * @throws IllegalArgumentException if the ID is invalid
     * @throws IllegalStateException if the deal cannot be submitted in its current state
     */
    Deal submitForApproval(String id);
    
    /**
     * Approves a deal, transitioning it to the APPROVED state.
     *
     * @param id the ID of the deal to approve (must not be null or empty)
     * @param approverUserId the ID of the user approving the deal (must not be null or empty)
     * @return the approved deal
     * @throws DealNotFoundException if the deal is not found
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws IllegalStateException if the deal cannot be approved in its current state
     */
    Deal approveDeal(String id, String approverUserId);
    
    /**
     * Rejects a deal, transitioning it to the REJECTED state.
     *
     * @param id the ID of the deal to reject (must not be null or empty)
     * @param rejectorUserId the ID of the user rejecting the deal (must not be null or empty)
     * @param reason the reason for rejection (must not be null or empty)
     * @return the rejected deal
     * @throws DealNotFoundException if the deal is not found
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws IllegalStateException if the deal cannot be rejected in its current state
     */
    Deal rejectDeal(String id, String rejectorUserId, String reason);
    
    /**
     * Cancels a deal, transitioning it to the CANCELLED state.
     *
     * @param id the ID of the deal to cancel (must not be null or empty)
     * @param reason the reason for cancellation (must not be null or empty)
     * @return the cancelled deal
     * @throws DealNotFoundException if the deal is not found
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws IllegalStateException if the deal cannot be cancelled in its current state
     */
    Deal cancelDeal(String id, String reason);
    
    /**
     * Synchronizes deal data with Salesforce, updating both systems with the latest information.
     *
     * @param id the ID of the deal to synchronize (must not be null or empty)
     * @return the updated deal with synchronized data
     * @throws DealNotFoundException if the deal is not found
     * @throws IllegalArgumentException if the ID is invalid
     * @throws SalesforceIntegrationException if there is an error communicating with Salesforce
     */
    Deal syncWithSalesforce(String id);
    
    /**
     * Updates deal data from Salesforce using the opportunity ID.
     *
     * @param opportunityId the Salesforce opportunity ID (must not be null or empty)
     * @return the updated deal with data from Salesforce
     * @throws DealNotFoundException if no matching deal is found
     * @throws IllegalArgumentException if the opportunity ID is invalid
     * @throws SalesforceIntegrationException if there is an error communicating with Salesforce
     */
    Deal updateFromSalesforce(String opportunityId);
    
    /**
     * Validates that a Salesforce opportunity exists and meets system requirements.
     *
     * @param opportunityId the Salesforce opportunity ID to validate (must not be null or empty)
     * @return true if the opportunity is valid, false otherwise
     * @throws IllegalArgumentException if the opportunity ID is invalid
     * @throws SalesforceIntegrationException if there is an error communicating with Salesforce
     */
    boolean validateSalesforceOpportunity(String opportunityId);
    
    /**
     * Synchronizes pricing information between the deal and Salesforce.
     *
     * @param id the ID of the deal to synchronize pricing for (must not be null or empty)
     * @throws DealNotFoundException if the deal is not found
     * @throws IllegalArgumentException if the ID is invalid
     * @throws SalesforceIntegrationException if there is an error communicating with Salesforce
     */
    void syncPricing(String id);
    
    /**
     * Updates the status of multiple deals in a single batch operation.
     *
     * @param ids list of deal IDs to update (must not be null or empty)
     * @param newStatus the new status to apply to all deals (must not be null)
     * @return list of updated deals
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws BatchOperationException if the operation fails for any deals
     */
    List<Deal> batchUpdateStatus(List<String> ids, DealStatus newStatus);
    
    /**
     * Synchronizes multiple deals with Salesforce in a single batch operation.
     *
     * @param ids list of deal IDs to synchronize (must not be null or empty)
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws BatchOperationException if the operation fails for any deals
     * @throws SalesforceIntegrationException if there is an error communicating with Salesforce
     */
    void batchSyncWithSalesforce(List<String> ids);
    
    /**
     * Counts the number of deals with a specific status.
     *
     * @param status the deal status to count (must not be null)
     * @return the count of deals with the specified status
     * @throws IllegalArgumentException if status is null
     */
    long countDealsByStatus(DealStatus status);
    
    /**
     * Calculates the total value of all deals with a specific status.
     *
     * @param status the deal status to calculate total value for (must not be null)
     * @return the total value as a BigDecimal
     * @throws IllegalArgumentException if status is null
     */
    BigDecimal calculateTotalValue(DealStatus status);
    
    /**
     * Finds deals that have expired before the specified date.
     *
     * @param expirationDate the cutoff date for expiration (must not be null)
     * @return list of expired deals
     * @throws IllegalArgumentException if expirationDate is null
     */
    List<Deal> findExpiredDeals(ZonedDateTime expirationDate);
}