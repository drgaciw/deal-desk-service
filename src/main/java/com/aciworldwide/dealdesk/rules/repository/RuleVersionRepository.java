package com.aciworldwide.dealdesk.rules.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.aciworldwide.dealdesk.rules.model.RuleVersion;

@Repository
public interface RuleVersionRepository extends MongoRepository<RuleVersion, String> {
    List<RuleVersion> findByRuleIdOrderByVersionDesc(String ruleId);
    Optional<RuleVersion> findByRuleIdAndVersion(String ruleId, int version);
    Optional<RuleVersion> findFirstByRuleIdOrderByVersionDesc(String ruleId);
} 