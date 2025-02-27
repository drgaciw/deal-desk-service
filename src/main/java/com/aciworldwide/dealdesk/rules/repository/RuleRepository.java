package com.aciworldwide.dealdesk.rules.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aciworldwide.dealdesk.rules.model.DealRule;

@Repository
public interface RuleRepository extends MongoRepository<DealRule, String> {
    // Add custom query methods if needed
} 