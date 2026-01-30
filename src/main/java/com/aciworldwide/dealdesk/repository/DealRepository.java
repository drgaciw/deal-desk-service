package com.aciworldwide.dealdesk.repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;

@Repository
public interface DealRepository extends MongoRepository<Deal, String> {
    
    Optional<Deal> findBySalesforceOpportunityId(String opportunityId);
    
    List<Deal> findByStatus(DealStatus status);

    long countByStatus(DealStatus status);
    
    List<Deal> findByAccountId(String accountId);
    
    List<Deal> findBySalesRepId(String salesRepId);
    
    @Query("{ 'value': { $gte: ?0 }, 'status': ?1 }")
    List<Deal> findHighValueDeals(BigDecimal minValue, DealStatus status);
    
    @Query("{ 'createdAt': { $gte: ?0 }, 'status': { $in: ?1 } }")
    List<Deal> findRecentDeals(ZonedDateTime since, List<DealStatus> statuses);
    
    boolean existsBySalesforceOpportunityId(String opportunityId);
}