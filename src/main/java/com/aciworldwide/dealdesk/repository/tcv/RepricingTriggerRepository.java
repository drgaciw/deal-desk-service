package com.aciworldwide.dealdesk.repository.tcv;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aciworldwide.dealdesk.model.tcv.RepricingTriggers;

@Repository
public interface RepricingTriggerRepository extends MongoRepository<RepricingTriggers, String> {
    // Add custom query methods if needed
} 