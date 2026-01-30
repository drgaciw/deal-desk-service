package com.aciworldwide.dealdesk.repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;

@Repository
public interface DealRepository extends MongoRepository<Deal, String> {
    
    Optional<Deal> findBySalesforceOpportunityId(String opportunityId);
    
    List<Deal> findByStatus(DealStatus status);

    Page<Deal> findByStatus(DealStatus status, Pageable pageable);
    
    List<Deal> findByAccountId(String accountId);

    Page<Deal> findByAccountId(String accountId, Pageable pageable);
    
    List<Deal> findBySalesRepId(String salesRepId);

    Page<Deal> findBySalesRepId(String salesRepId, Pageable pageable);
    
    @Query("{ 'value': { $gte: ?0 }, 'status': ?1 }")
    List<Deal> findHighValueDeals(BigDecimal minValue, DealStatus status);

    @Query("{ 'value': { $gte: ?0 }, 'status': ?1 }")
    Page<Deal> findHighValueDeals(BigDecimal minValue, DealStatus status, Pageable pageable);
    
    @Query("{ 'createdAt': { $gte: ?0 }, 'status': { $in: ?1 } }")
    List<Deal> findRecentDeals(ZonedDateTime since, List<DealStatus> statuses);

    @Query("{ 'createdAt': { $gte: ?0 }, 'status': { $in: ?1 } }")
    Page<Deal> findRecentDeals(ZonedDateTime since, List<DealStatus> statuses, Pageable pageable);
    
    boolean existsBySalesforceOpportunityId(String opportunityId);
}