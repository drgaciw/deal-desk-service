package com.aciworldwide.dealdesk.repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;

@Repository
public interface DealRepository extends MongoRepository<Deal, String> {
    
    @Aggregation(pipeline = {
        "{ $match: { status: ?0 } }",
        "{ $group: { _id: null, total: { $sum: '$value' } } }"
    })
    TotalValueResult calculateTotalValueByStatus(DealStatus status);

    Optional<Deal> findBySalesforceOpportunityId(String opportunityId);
    
    List<Deal> findByStatus(DealStatus status);
    
    List<Deal> findByAccountId(String accountId);
    
    List<Deal> findBySalesRepId(String salesRepId);
    
    @Query("{ 'value': { $gte: ?0 }, 'status': ?1 }")
    List<Deal> findHighValueDeals(BigDecimal minValue, DealStatus status);
    
    @Query("{ 'createdAt': { $gte: ?0 }, 'status': { $in: ?1 } }")
    List<Deal> findRecentDeals(ZonedDateTime since, List<DealStatus> statuses);
    
    boolean existsBySalesforceOpportunityId(String opportunityId);
}